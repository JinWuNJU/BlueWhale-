package com.seecoder.BlueWhale.serviceImpl;

import com.seecoder.BlueWhale.aspect.AuthLevelCheck;
import com.seecoder.BlueWhale.enums.CouponListSortType;
import com.seecoder.BlueWhale.enums.CouponStatus;
import com.seecoder.BlueWhale.enums.CouponType;
import com.seecoder.BlueWhale.enums.RoleEnum;
import com.seecoder.BlueWhale.exception.BlueWhaleException;
import com.seecoder.BlueWhale.po.*;
import com.seecoder.BlueWhale.repository.CouponGroupRepository;
import com.seecoder.BlueWhale.repository.CouponRepository;
import com.seecoder.BlueWhale.repository.StoreRepository;
import com.seecoder.BlueWhale.service.CouponService;
import com.seecoder.BlueWhale.serviceImpl.couponStrategy.Context;
import com.seecoder.BlueWhale.util.RandomUtil;
import com.seecoder.BlueWhale.util.SecurityUtil;
import com.seecoder.BlueWhale.vo.CouponGroupListVO;
import com.seecoder.BlueWhale.vo.CouponGroupVO;
import com.seecoder.BlueWhale.vo.CouponVO;
import com.seecoder.BlueWhale.vo.OrderPriceCalculateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CouponServiceImpl implements CouponService {
    @Autowired
    CouponGroupRepository couponGroupRepository;
    @Autowired
    SecurityUtil securityUtil;
    @Autowired
    RandomUtil randomUtil;
    @Autowired
    CouponRepository couponRepository;
    @Autowired
    StoreRepository storeRepository;

    @Override
    public Coupon getCouponByIdForCustomer(String couponGroupId, User user) {
        CouponGroup cg = couponGroupRepository.findByCouponGroupId(couponGroupId);
        if (cg == null)
            throw new BlueWhaleException("不存在的优惠券组");
        return getCouponByIdForCustomer(cg, user);
    }

    private Coupon getCouponByIdForCustomer(CouponGroup couponGroup, User user) {
        Coupon coupon = couponRepository.findByCouponGroupAndUser(couponGroup, user);
        if (coupon == null) {
            throw new BlueWhaleException("未拥有该优惠券");
        }
        return coupon;
    }

    @Override
    @AuthLevelCheck(roleGroup = {RoleEnum.CEO, RoleEnum.STAFF})
    public CouponGroupVO createGroup(com.seecoder.BlueWhale.vo.CouponGroupVO groupVO) {
        User user = securityUtil.getCurrentUser();
        Store store = null;
        if (user.getRole() == RoleEnum.STAFF) {
            store = user.getStore();
        }
        if (groupVO.getCouponType() == CouponType.FULL_REDUCTION) {
            if (groupVO.getDiscountAmount() > groupVO.getThresholdAmount()) {
                throw new BlueWhaleException("满减折扣金额不可大于满减门槛");
            }
        }
        if (groupVO.getEndTime() != null && groupVO.getEndTime().before(new Date())) {
            throw new BlueWhaleException("活动结束时间不能早于当前时间");
        }
        if (groupVO.getEndTime() != null && groupVO.getStartTime() != null && groupVO.getStartTime().after(groupVO.getEndTime())) {
            throw new BlueWhaleException("活动开始时间不能晚于结束时间");
        }
        CouponGroup couponGroup = groupVO.toPO(store, user);
        String randomId = randomUtil.generateUniqueId(v -> couponGroupRepository.findById(v).isPresent());
        couponGroup.setCouponGroupId(randomId);
        couponGroupRepository.save(couponGroup);
        return couponGroup.toVO();
    }

    /**
     * 顾客使用优惠，不检查优惠券可用性（应在计算价格和绑定优惠券到订单时检查）
     */
    @Override
    public void useCoupon(Coupon coupon, OrderInfo orderInfo) {
        CouponGroup couponGroup = coupon.getCouponGroup();
        couponGroup.setUsedCount(couponGroup.getUsedCount() + 1);
        coupon.setCouponStatus(CouponStatus.USED);
        coupon.setOrderInfo(orderInfo);
        coupon.setDisabledTime(new Date());
        orderInfo.setCoupon(coupon);
        couponRepository.save(coupon);
    }

    @Override
    public void refundCoupon(OrderInfo orderInfo) {
        Coupon coupon = orderInfo.getCoupon();
        if (coupon == null) return;
        CouponGroup couponGroup = coupon.getCouponGroup();
        couponGroup.setUsedCount(couponGroup.getUsedCount() - 1);
        if (isValidTime(coupon)) {
            coupon.setCouponStatus(CouponStatus.UNUSED);
            coupon.setDisabledTime(null);
        } else {
            coupon.setCouponStatus(CouponStatus.EXPIRED);
        }
        coupon.setOrderInfo(null);
        orderInfo.setCoupon(null);
        couponRepository.save(coupon);
    }

    /**
     * 验证优惠券是否未过期
     */
    @Override
    public boolean isValidTime(Coupon coupon) {
        CouponGroup couponGroup = coupon.getCouponGroup();
        if (couponGroup.getEndTime() != null && couponGroup.getEndTime().before(new Date())) {
            if (coupon.getCouponStatus() == CouponStatus.UNUSED)
                expireCouponGroup(couponGroup); // 清理过期而还是UNUSED状态的券
            return false;
        }
        return true;
    }

    private void expireCouponGroup(CouponGroup couponGroup) {
        couponRepository.findByCouponGroupAndCouponStatus(couponGroup, CouponStatus.UNUSED).forEach(coupon -> coupon.setCouponStatus(CouponStatus.EXPIRED));
    }


    /**
     * 顾客视角下，在CouponGroupVO上附加该顾客所持有的该组优惠券的优惠券状态
     *
     * @param customerCoupons 顾客所持有的优惠券列表
     * @param couponGroups    优惠券组列表
     * @return 附加状态的CouponGroupVO列表
     */
    private ArrayList<CouponGroupVO> appendCouponStatus(List<Coupon> customerCoupons, List<CouponGroup> couponGroups) {
        List<Coupon> unmatchedCouponList = new ArrayList<>(customerCoupons);
        ArrayList<CouponGroupVO> result = new ArrayList<>();
        Date now = new Date();
        Date ignoreBefore = generateQueryDateConditionForCoupon();
        for (CouponGroup couponGroup : couponGroups) {
            CouponGroupVO vo = couponGroup.toVO();
            // 计算未领取时的顾客视角的优惠券状态
            if (couponGroup.getStartTime().after(now)) { // 活动未开始
                vo.setCouponStatus(CouponStatus.PENDING);
                result.add(vo);
                continue;
            } else if (couponGroup.getEndTime().before(now)) {
                vo.setCouponStatus(CouponStatus.EXPIRED);
                result.add(vo);
                continue;
            }
            if (couponGroup.getRemainingCount() > 0)
                vo.setCouponStatus(CouponStatus.UNCLAIMED); // 活动开始，但未领取;或活动已过期，但未领取过
            else
                vo.setCouponStatus(CouponStatus.EMPTY);
            // 若已领取则优惠券状态为coupon.coupon_status
            for (Coupon unmatchedCoupon : unmatchedCouponList) {
                if (couponGroup.getCouponGroupId().equals(unmatchedCoupon.getCouponGroup().getCouponGroupId())) {  // 已领取，返回领取后优惠券状态
                    if (!isValidTime(unmatchedCoupon)) { // 确保不存在过期而还是UNUSED的券
                        vo.setCouponStatus(CouponStatus.EXPIRED);
                    } else {
                        if (unmatchedCoupon.getDisabledTime() != null && unmatchedCoupon.getDisabledTime().before(ignoreBefore))
                            vo = null; // 忽略掉一段时间以前已经使用过的券
                        else
                            vo.setCouponStatus(unmatchedCoupon.getCouponStatus());
                    }
                    unmatchedCouponList.remove(unmatchedCoupon);
                    break;
                }
            }
            if (vo != null)
                result.add(vo);
        }
        return result;
    }

    @Override
    public CouponVO claim(String couponGroupId) {
        Optional<CouponGroup> couponGroupData = couponGroupRepository.findById(couponGroupId);
        if (!couponGroupData.isPresent()) {
            throw new BlueWhaleException("优惠券不存在");
        }
        CouponGroup couponGroup = couponGroupData.get();
        Date date = new Date();
        if (couponGroup.getRemainingCount() == 0) {
            throw new BlueWhaleException("优惠券已经被领完");
        }
        if ((couponGroup.getEndTime() != null && date.after(couponGroup.getEndTime())) || date.before(couponGroup.getStartTime())) {
            throw new BlueWhaleException("未到活动时间");
        }
        User user = securityUtil.getCurrentUser();
        if (couponRepository.findByCouponGroupAndUser(couponGroup, user) != null) {
            throw new BlueWhaleException("已领过该优惠券");
        }
        Coupon coupon = new Coupon();
        coupon.setUser(user);
        coupon.setCouponGroup(couponGroup);
        coupon.setStore(couponGroup.getStore());
        coupon.setDisabledTime(couponGroup.getEndTime());
        coupon.setCouponStatus(CouponStatus.UNUSED);
        couponGroup.setRemainingCount(couponGroup.getRemainingCount() - 1);
        couponRepository.save(coupon);
        return coupon.toVO();
    }

    /**
     * 生成查询优惠券的日期条件
     *
     * @return 当前时间前七天的日期的Date对象
     */
    private Date generateQueryDateConditionForCoupon() {
        LocalDateTime queryCouponsAfter = LocalDateTime.now();
        queryCouponsAfter = queryCouponsAfter.minusDays(7);
        return Date.from(queryCouponsAfter.atZone(ZoneId.systemDefault()).toInstant());
    }

    @Override
    public List<CouponGroupVO> getCouponGroupsOfStoreForCustomer(Set<Store> storeSet, User user) {
        Date dateCondition = generateQueryDateConditionForCoupon();
        List<CouponGroup> couponGroups = couponGroupRepository.findCouponGroupOfStoreForCustomer(storeSet, dateCondition);
        if (couponGroups.isEmpty())
            return new ArrayList<>();
        List<Coupon> coupons = couponRepository.findByUserAndStoreAndCouponGroupIn(user, storeSet, couponGroups);
        List<CouponGroupVO> couponGroupVOS = appendCouponStatus(coupons, couponGroups);
        couponGroupVOS.forEach(CouponGroupVO::omitManagementData);
        return couponGroupVOS;
    }

    @Override
    @AuthLevelCheck(roleGroup = {RoleEnum.STAFF, RoleEnum.CUSTOMER, RoleEnum.CEO})
    public CouponGroupListVO list(int page, Integer storeId, CouponListSortType sort) {
        Pageable pageRequest = PageRequest.of(page, 20, Sort.by(Sort.Order.desc("endTime")));
        User user = securityUtil.getCurrentUser();
        if (user.getRole() == RoleEnum.CUSTOMER) {
            if (storeId != null) {
                Store store = storeRepository.findByStoreId(storeId);
                if (store != null) {
                    HashSet<Store> storeSet = new HashSet<>();
                    storeSet.add(store);
                    List<CouponGroupVO> couponGroupVOS = getCouponGroupsOfStoreForCustomer(storeSet, user);
                    return new CouponGroupListVO(couponGroupVOS, 1); // 单个商店内优惠券组数量不会很大，直接一次查询全部并返回
                } else {
                    throw new BlueWhaleException("非法的storeId");
                }
            } else {
                Date dateCondition = generateQueryDateConditionForCoupon();
                Page<CouponGroup> pageOfCouponGroups = couponGroupRepository.findAllByEndTimeIsAfter(pageRequest, dateCondition);
                List<CouponGroup> couponGroups = pageOfCouponGroups.toList();
                if (pageOfCouponGroups.isEmpty())
                    return new CouponGroupListVO(new ArrayList<>(), 0);
                List<CouponGroupVO> couponGroupVOS = appendCouponStatus(couponRepository.findByUserAndCouponGroupIn(user, couponGroups), couponGroups);
                couponGroupVOS.forEach(CouponGroupVO::omitManagementData);
                return new CouponGroupListVO(couponGroupVOS, pageOfCouponGroups.getTotalPages());
            }
        }
        // 顾客触发的业务逻辑全部结束并return，下面代码处理Manager和CEO
        // 对于Manager和CEO无需关心couponStatus
        if (sort == CouponListSortType.createTime)
            pageRequest = PageRequest.of(page, 20, Sort.by(Sort.Order.desc("createTime")));
        else if (sort == CouponListSortType.endTime)
            pageRequest = PageRequest.of(page, 20, Sort.by(Sort.Order.desc("endTime")));
        Page<CouponGroup> result = null;
        if (user.getRole() == RoleEnum.CEO) {
            if (storeId != null) {
                Store store = storeRepository.findByStoreId(storeId);
                if (store != null) {
                    result = couponGroupRepository.findByStore(pageRequest, store);
                } else {
                    throw new BlueWhaleException("非法的storeId");
                }
            } else {// 此时findall
                result = couponGroupRepository.findBy(pageRequest);
            }
        }
        if (user.getRole() == RoleEnum.STAFF) {   //对于店员的自身StoreId寻找即可
            if (storeId != null) {
                throw new BlueWhaleException("店员不应设置storeId参数");
            }
            result = couponGroupRepository.findByStore(pageRequest, user.getStore());
        }
        if (result == null) {
            throw new BlueWhaleException("生成优惠券列表错误");
        }
        return new CouponGroupListVO(result.stream().map(CouponGroup::toVO).collect(Collectors.toList()), result.getTotalPages());
    }

    @Override
    public Coupon getCouponThatIsValid(OrderPriceCalculateVO order, User user) {
        Coupon coupon = getCouponByIdForCustomer(order.getCouponGroupId(), user);
        if (coupon.getStore() != null && !Objects.equals(coupon.getStore().getStoreId(), order.getSku().getItem().getStore().getStoreId()))
            throw new BlueWhaleException("非本店优惠券");
        if (!isValidTime(coupon))
            throw new BlueWhaleException("优惠券不可用");
        Context couponContext = Context.getContext(coupon.getCouponGroup().toVO());
        if (couponContext.valid(order.getTotal_amount_original())) {
            return coupon;
        } else {
            throw new BlueWhaleException("不满足使用条件");
        }
    }

    /**
     * 计算用券后价格
     *
     * @param coupon
     * @param price
     * @return
     */
    @Override
    public double applyCouponOnPrice(Coupon coupon, double price) {
        return Context.getContext(coupon.getCouponGroup().toVO()).calculate(price);
    }

    @Override
    public List<OrderPriceCalculateVO> getCouponListForOrders(List<OrderPriceCalculateVO> orders, User user) {
        HashMap<Integer, Store> stores = new HashMap<>();
        for (OrderPriceCalculateVO o : orders) {
            stores.put(o.getSkuId(), o.getSku().getItem().getStore());
            o.setCouponGroup(new ArrayList<>());
            o.setTotal_amount_max_discount(o.getTotal_amount_original());
        }
        List<CouponGroupVO> couponGroupVOList = getCouponGroupsOfStoreForCustomer(new HashSet<>(stores.values()), user);
        // 购买时不关心未开启的促销活动或过期优惠券
        couponGroupVOList = couponGroupVOList.stream().filter(
                        cg -> cg.getCouponStatus() == CouponStatus.UNCLAIMED
                                || cg.getCouponStatus() == CouponStatus.UNUSED)
                .collect(Collectors.toList());
        for (CouponGroupVO cg : couponGroupVOList) {
            Context couponContext = Context.getContext(cg);
            for (OrderPriceCalculateVO o : orders) {
                if (cg.getStoreId() != null && !cg.getStoreId().equals(o.getSku().getItem().getStore().getStoreId()))
                    continue;
                CouponGroupVO couponGroupVO = cg.clone();
                if (couponContext.valid(o.getTotal_amount_original())) {
                    double discountedPrice = couponContext.calculate(o.getTotal_amount_original());
                    if (Objects.equals(o.getCouponGroupId(), couponGroupVO.getCouponGroupId())) {
                        o.setTotal_amount(discountedPrice); // 顾客主动选择的优惠券价格
                    }
                    couponGroupVO.setDiscountAmountForOrder(o.getTotal_amount_original() - discountedPrice);
                } else {
                    couponGroupVO.setCouponStatus(CouponStatus.UNUSABLE);
                }
                o.getCouponGroup().add(couponGroupVO);
            }
        }
        // 按优惠力度排序
        for (OrderPriceCalculateVO o : orders) {
            o.getCouponGroup().sort((a, b) -> Double.compare(b.getDiscountAmountForOrder(), a.getDiscountAmountForOrder()));
        }
        HashMap<String, OrderPriceCalculateVO> recommendations = new HashMap<>();  // 记录优惠券在多个订单上可用时，最佳的用法
        Queue<OrderPriceCalculateVO> queue = new LinkedList<>(orders);
        while (!queue.isEmpty()) {
            OrderPriceCalculateVO o = queue.poll();
            CouponGroupVO betterCoupon = null;
            for (CouponGroupVO cg : o.getCouponGroup()) {
                if (recommendations.get(cg.getCouponGroupId()) == null) {
                    if (cg.getDiscountAmountForOrder() > 0)
                        betterCoupon = cg;
                    break;
                } else {
                    OrderPriceCalculateVO using = recommendations.get(cg.getCouponGroupId());
                    double usingDiscount = using.getTotal_amount_original() - using.getTotal_amount_max_discount();
                    if (cg.getDiscountAmountForOrder() > usingDiscount) {
                        o.setMaxDiscountCoupon(null);
                        o.setTotal_amount_max_discount(o.getTotal_amount_original());
                        queue.add(o);
                        break;
                    }
                }
            }
            if (betterCoupon != null) {
                o.setMaxDiscountCoupon(betterCoupon.getCouponGroupId());
                o.setTotal_amount_max_discount(o.getTotal_amount_original() - betterCoupon.getDiscountAmountForOrder());
                recommendations.put(betterCoupon.getCouponGroupId(), o);
            }
        }
        return orders;
    }
}
