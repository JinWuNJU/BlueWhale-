package com.seecoder.BlueWhale.serviceImpl;

import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.seecoder.BlueWhale.aspect.AuthLevelCheck;
import com.seecoder.BlueWhale.configure.AlipayTools;
import com.seecoder.BlueWhale.enums.*;
import com.seecoder.BlueWhale.exception.BlueWhaleException;
import com.seecoder.BlueWhale.po.*;
import com.seecoder.BlueWhale.repository.ExpressInfoRepository;
import com.seecoder.BlueWhale.repository.OrderRepository;
import com.seecoder.BlueWhale.repository.ReviewRepository;
import com.seecoder.BlueWhale.repository.StoreRepository;
import com.seecoder.BlueWhale.service.*;
import com.seecoder.BlueWhale.util.OssUtil;
import com.seecoder.BlueWhale.util.RandomUtil;
import com.seecoder.BlueWhale.util.SecurityUtil;
import com.seecoder.BlueWhale.vo.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    ReviewRepository reviewRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    SecurityUtil securityUtil;
    @Autowired
    CouponService couponService;
    @Autowired
    AlipayTools alipayTools;
    @Autowired
    RandomUtil randomUtil;
    @Autowired
    StoreRepository storeRepository;
    @Autowired
    OssUtil ossUtil;
    @Autowired
    ExpressInfoRepository expressInfoRepository;
    @Autowired
    ItemService itemService;
    @Autowired
    CartService cartService;
    @Autowired
    UserService userService;

    @AuthLevelCheck(roleGroup = {RoleEnum.STAFF, RoleEnum.CEO, RoleEnum.MANAGER})
    @Override
    public String export(String storeId, Date startTime, Date endTime) {
        List<OrderInfo> list;
        User user = securityUtil.getCurrentUser();
        Store store = null;
        if (user.getRole() == RoleEnum.CEO || user.getRole() == RoleEnum.MANAGER) {
            if (storeId != null) {
                store = storeRepository.findByStoreId(Integer.parseInt(storeId));
                if (store == null) {
                    throw new BlueWhaleException("StoreId不存在");
                }
            }
        } else if (user.getRole() == RoleEnum.STAFF) {
            if (storeId != null) {
                throw new BlueWhaleException("店员不应设置storeId参数");
            }
            store = user.getStore();
        }
        if (store == null) {
            if (startTime == null && endTime == null) {
                list = orderRepository.findAll();
            } else if (startTime == null && endTime != null) {
                list = orderRepository.findByOrderTimeBefore(endTime);
            } else if (startTime != null && endTime == null) {
                list = orderRepository.findByOrderTimeAfter(startTime);
            } else {
                list = orderRepository.findByOrderTimeBetween(startTime, endTime);
            }
        } else {
            if (startTime == null && endTime == null) {
                list = orderRepository.findByStore(store);
            } else if (startTime == null && endTime != null) {
                list = orderRepository.findByStoreAndOrderTimeBefore(store, endTime);
            } else if (startTime != null && endTime == null) {
                list = orderRepository.findByStoreAndOrderTimeAfter(store, startTime);
            } else {
                list = orderRepository.findByStoreAndOrderTimeBetween(store, startTime, endTime);
            }
        }
        String url = "";
        try {
            url = toExcel(list);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return url;
    }

    @Override
    @AuthLevelCheck
    public Boolean cancel(String tradeNo) {
        OrderInfo order = getOrder(tradeNo);
        if (order.getOrderStatus() != OrderStatus.UNPAID && order.getOrderStatus() != OrderStatus.TOPAY)
            throw new BlueWhaleException("非可取消订单");
        if (isOrderACartTrade(order)) {
            cartService.disableCartTrade(order.getCartTrade());
        }
        if (order.getOrderStatus() == OrderStatus.TOPAY) {
            if (payStatus(order) == TradeStatus.TRADE_SUCCESS)
                throw new BlueWhaleException("已支付过，请使用退款流程");
            couponService.refundCoupon(order);
        }
        order.setOrderStatus(OrderStatus.CANCEL);
        orderRepository.save(order);
        itemService.refund(order.getSku(), order.getCount());
        return true;
    }

    private boolean isOrderACartTrade(OrderInfo order) {
        return order.getCartTrade() != null;
    }

    private void tradeRefund(OrderInfo order) {
        AlipayTradeRefundResponse response = alipayTools.refund(order.getTradeNoAli(), order.getTotalAmount());
        if (response.getFundChange() != null && response.getFundChange().equals("Y")) {
            couponService.refundCoupon(order);
            order.setOrderStatus(OrderStatus.REFUNDED);
            orderRepository.save(order);
            itemService.refund(order.getSku(), order.getCount());
            return;
        }
        throw new BlueWhaleException("退款异常");
    }

    @Override
    @AuthLevelCheck(roleGroup = {RoleEnum.CUSTOMER, RoleEnum.STAFF})
    public Boolean refund(String tradeNo, ExpressInfoVO expressInfo) {
        OrderInfo order = getOrder(tradeNo);
        User user = securityUtil.getCurrentUser();

        if (order.getOrderStatus() == OrderStatus.UNSEND) {
            tradeRefund(order);
        } else if (order.getOrderStatus() == OrderStatus.UNGET) {
            order.setOrderStatus(OrderStatus.REFUNDING);
            if (expressInfo.getMailNo() != null) {
                ExpressInfo e = expressInfo.toPO();
                expressInfoRepository.save(e);
                order.setRefundExpress(e);
            }
            orderRepository.save(order);
        } else if (order.getOrderStatus() == OrderStatus.REFUNDING && user.getRole() == RoleEnum.STAFF) {
            tradeRefund(order);
        } else {
            throw new BlueWhaleException("非可退款订单");
        }
        return true;
    }

    private String toExcel(List<OrderInfo> list) throws IOException {
        String extension = "xlsx";
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("订单报表");

        Row headerRow = sheet.createRow(0);
        String[] headers = {"订单ID", "商品名称", "SKU名称", "购买数量", "交易金额", "配送方式", "订单状态", "下单时间", "交易人", "交易人电话"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        for (int i = 0; i < list.size(); i++) {
            OrderInfo orderInfo = list.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(orderInfo.getOrderId());
            row.createCell(1).setCellValue(orderInfo.getItemName());
            row.createCell(2).setCellValue(orderInfo.getSkuName());
            row.createCell(3).setCellValue(orderInfo.getCount());
            row.createCell(4).setCellValue(orderInfo.getTotalAmount());
            row.createCell(5).setCellValue(orderInfo.getShipping().toString());
            row.createCell(6).setCellValue(orderInfo.getOrderStatus().toString());
            row.createCell(7).setCellValue(orderInfo.getOrderTime().toString());
            row.createCell(8).setCellValue(orderInfo.getUserName());
            row.createCell(9).setCellValue(orderInfo.getUserPhone());
        }
        try {
            return ossUtil.upload(String.format("报表-%s.%s", new Date(), extension), workbookToInputStream(workbook));
        } catch (Exception e) {
            throw new BlueWhaleException("生成报表异常");
        }
    }

    private static InputStream workbookToInputStream(Workbook workbook) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        workbook.close();
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return new ByteArrayInputStream(byteArray);
    }

    @Override
    public ReviewVO review(String tradeNo, ReviewVO review) {
        OrderInfo orderInfo = orderRepository.findByTradeNo(tradeNo);
        if (orderInfo == null)
            throw new BlueWhaleException("订单不存在");
        User user = securityUtil.getCurrentUser();
        if (!orderInfo.getUser().getId().equals(user.getId()))
            throw new BlueWhaleException("非本人订单");
        if (orderInfo.getOrderStatus() != OrderStatus.UNCOMMENT)
            throw new BlueWhaleException("非可评价订单");
        Sku sku = orderInfo.getSku();
        // 更新商品评分
        Item item = sku.getItem();
        double itemRating = item.getItemRating();
        int itemReviewCount = item.getItemReviewCount();
        itemRating = itemRating + (review.getRating() - itemRating) / (itemReviewCount + 1);
        item.setItemReviewCount(itemReviewCount + 1);
        item.setItemRating(itemRating);
        // 更新商店评分
        Store store = item.getStore();
        double storeRating = store.getStoreRating();
        int storeReviewCount = store.getStoreReviewCount();
        storeRating = storeRating + (review.getRating() - storeRating) / (storeReviewCount + 1);
        store.setStoreReviewCount(storeReviewCount + 1);
        store.setStoreRating(storeRating);

        Review reviewPO = review.toPO(orderInfo, user, item, sku);
        orderInfo.setOrderStatus(OrderStatus.DONE);
        reviewRepository.save(reviewPO);
        return reviewPO.toVO();
    }

    @Override
    public OrderInfo create(int count, User user, Sku sku, Long addressId, Shipping shipping) {
        if (count > sku.getSkuLimitPerCustomer())
            throw new BlueWhaleException("超出单次购买限制");
        Item item = sku.getItem();
        Store store = item.getStore();
        OrderPriceCalculateVO orderPriceInfo = new OrderPriceCalculateVO(sku, count, null);
        calculatePrice(false, orderPriceInfo, user);

        OrderInfo order = new OrderInfo();

        if (shipping == Shipping.DELIVERY) {
            if (addressId == null) {
                order.setAddress(user.getDefaultAddress());
            } else {
                Address address = userService.getAddress(addressId, user);
                order.setAddress(address);
            }
        }
        order.setUser(user);

        order.setSku(sku);
        order.setCount(orderPriceInfo.getCount());
        itemService.buy(sku, order.getCount());
        order.setShipping(shipping);
        order.setOrderStatus(OrderStatus.UNPAID);
        order.setTotalAmount(orderPriceInfo.getTotal_amount_original());
        order.setTotalAmountOriginal(orderPriceInfo.getTotal_amount_original());

        order.setStoreName(store.getStoreName());
        order.setItemName(item.getItemName());
        order.setSkuName(sku.getSkuName());
        order.setStore(item.getStore());
        order.setItem(sku.getItem());
        order.setTradeNo(randomUtil.generateUniqueId(v -> orderRepository.findByTradeNo(v) != null));
        Optional<ImageInfo> itemImage = item.getItemImage().stream().findFirst();
        itemImage.ifPresent(order::setItemImage);
        orderRepository.save(order);
        return order;
    }

    @Override
    public OrderVO create(OrderPriceCalculateVO orderInfo) {
        User user = securityUtil.getCurrentUser();
        Sku sku = itemService.getSkuById(orderInfo.getSkuId());
        OrderInfo order = create(orderInfo.getCount(), user, sku, orderInfo.getAddressId(), orderInfo.getShipping());
        return order.toVO();
    }

    public Boolean dispatch(OrderVO order) {
        OrderInfo orderInfo = orderRepository.findByTradeNo(order.getOrderId());
        if (orderInfo == null)
            throw new BlueWhaleException("订单不存在");
        User user = securityUtil.getCurrentUser();
        if (!(user.getRole() == RoleEnum.STAFF)) {
            throw new BlueWhaleException("非工作人员无权限");
        }
        Store store = user.getStore();
        if (!store.getStoreId().equals(orderInfo.getStore().getStoreId())) {
            throw new BlueWhaleException("非本商店订单");
        }
        if (orderInfo.getOrderStatus() != OrderStatus.UNSEND) {
            throw new BlueWhaleException("非可发货订单");
        }
        orderInfo.setOrderStatus(OrderStatus.UNGET);
        if (orderInfo.getShipping() == Shipping.DELIVERY) {
            if (order.getExpressNo() != null) {
                ExpressInfo expressInfo = new ExpressInfo(order.getExpressNo());
                expressInfoRepository.save(expressInfo);
                orderInfo.setExpress(expressInfo);
            }
        }
        orderRepository.save(orderInfo);
        return true;
    }

    public Boolean receive(OrderBasicVO order) {
        OrderInfo orderInfo = getOrder(order.getOrderId());
        if (orderInfo.getOrderStatus() != OrderStatus.UNGET) {
            throw new BlueWhaleException("非可收货订单");
        }
        orderInfo.setOrderStatus(OrderStatus.UNCOMMENT);
        orderRepository.save(orderInfo);
        return true;
    }

    /**
     * 计算价格
     * @param recommendCoupon 是否计算最佳优惠券
     * @param order 通过构造函数(sku, count, couponGroupId)创建的OrderPriceCalculateVO
     * @param user
     * @return
     */
    @Override
    public OrderPriceCalculateVO calculatePrice(boolean recommendCoupon, OrderPriceCalculateVO order, User user) {
        // 计算不含优惠券参与的价格
        int count = order.getCount();
        Sku sku = order.getSku();
        if (sku.getSkuInventory() < count)
            throw new BlueWhaleException("库存不足");
        if (recommendCoupon) {
            // 查询订单可用优惠券，用于支付时选择优惠券场景
            ArrayList<OrderPriceCalculateVO> orders = new ArrayList<>();
            orders.add(order);
            couponService.getCouponListForOrders(orders, user);
        } else {
            // 设置订单最终使用的优惠券和价格，发生在获取支付宝链接之前
            if (order.getCouponGroupId() != null && !order.getCouponGroupId().isEmpty()) {
                Coupon coupon = couponService.getCouponThatIsValid(order, user);
                order.setTotal_amount(couponService.applyCouponOnPrice(coupon, order.getTotal_amount_original()));
                order.setUsingCouponPO(coupon);
            }
        }
        return order;
    }

    @Override
    public OrderPriceCalculateVO calculate(OrderPriceCalculateVO order) {
        Sku sku = itemService.getSkuById(order.getSkuId());
        if (sku == null)
            throw new BlueWhaleException("skuId不存在");
        User user = securityUtil.getCurrentUser();
        OrderPriceCalculateVO orderPrice = new OrderPriceCalculateVO(sku, order.getCount(), order.getCouponGroupId());
        return calculatePrice(true, orderPrice, user);
    }

    @Override
    public OrderListVO list(int page, ListSortType sort, SortingOrder sortingOrder, OrderStatus[] orderFilter, Shipping shipping) {
        Pageable pageRequest;
        switch (sort) {
            case count:
                pageRequest = PageRequest.of(page, 20, Sort.by(sortingOrder.toDirection(), "count"));
                break;
            case price:
                pageRequest = PageRequest.of(page, 20, Sort.by(sortingOrder.toDirection(), "totalAmount"));
                break;
            case time:
            default:
                pageRequest = PageRequest.of(page, 20, Sort.by(sortingOrder.toDirection(), "orderTime"));
                break;
        }
        User user = securityUtil.getCurrentUser();
        Page<OrderInfo> result = null;
        if (orderFilter == null)
            orderFilter = new OrderStatus[]{};
        Set<OrderStatus> filters = Arrays.stream(orderFilter).collect(Collectors.toSet());
        if (user.getRole() == RoleEnum.CUSTOMER) {      // 顾客可查看自己产生的订单
            if (orderFilter.length == 0 && shipping == null)          // 不限订单状态、配送方式
                result = orderRepository.findByUser(pageRequest, user);
            else if (orderFilter.length > 0 && shipping == null)      // 限定订单状态、不限配送方式
                result = orderRepository.findByUserAndOrderStatusIn(pageRequest, user, filters);
            else if (orderFilter.length == 0 && shipping != null)     // 不限订单状态、限定配送方式
                result = orderRepository.findByUserAndShipping(pageRequest, user, shipping);
            else                                                      // 限定订单状态和配送方式
                result = orderRepository.findByUserAndShippingAndOrderStatusIn(pageRequest, user, shipping, filters);
        } else if (user.getRole() == RoleEnum.STAFF) {  // 店员可查看商店内订单
            Store store = user.getStore();
            if (orderFilter.length == 0 && shipping == null)
                result = orderRepository.findByStore(pageRequest, store);
            else if (orderFilter.length > 0 && shipping == null)
                result = orderRepository.findByStoreAndOrderStatusIn(pageRequest, store, filters);
            else if (orderFilter.length == 0 && shipping != null)
                result = orderRepository.findByStoreAndShipping(pageRequest, store, shipping);
            else
                result = orderRepository.findByStoreAndShippingAndOrderStatusIn(pageRequest, store, shipping, filters);
        } else if (user.getRole() == RoleEnum.CEO || user.getRole() == RoleEnum.MANAGER) {   // 经理和ceo可查看商场订单
            if (orderFilter.length == 0 && shipping == null)
                result = orderRepository.findAll(pageRequest);
            else if (orderFilter.length > 0 && shipping == null)
                result = orderRepository.findByOrderStatusIn(pageRequest, filters);
            else if (orderFilter.length == 0 && shipping != null)
                result = orderRepository.findByShipping(pageRequest, shipping);
            else
                result = orderRepository.findByShippingAndOrderStatusIn(pageRequest, shipping, filters);
        }
        if (result == null)
            throw new BlueWhaleException("生成订单列表错误");
        return new OrderListVO(result.stream().map(OrderInfo::toVO).collect(Collectors.toList()), result.getTotalPages());
    }

    @Override
    public OrderVO get(String tradeNo) {
        return getOrder(tradeNo).toVO();
    }

    private OrderInfo getOrder(String tradeNo) {
        User user = securityUtil.getCurrentUser();
        OrderInfo result = orderRepository.findByTradeNo(tradeNo);
        if (result == null)
            throw new BlueWhaleException("获取订单信息出错");
        if (user.getRole() == RoleEnum.CUSTOMER) {
            if (!result.getUser().getId().equals(user.getId()))
                throw new BlueWhaleException("获取订单信息出错");
        } else if (user.getRole() == RoleEnum.STAFF) {
            Store store = user.getStore();
            if (!store.getStoreId().equals(result.getStore().getStoreId()))
                throw new BlueWhaleException("获取订单信息出错");
        } else if (user.getRole() == RoleEnum.CEO || user.getRole() == RoleEnum.MANAGER) {

        } else {
            throw new BlueWhaleException("获取订单信息出错");
        }
        return result;
    }

    private TradeStatus payStatus(OrderInfo orderInfo) {
        if (orderInfo.getOrderStatus() == OrderStatus.UNPAID || orderInfo.getOrderStatus() == OrderStatus.TOPAY) {
            String outTradeNo = orderInfo.getTradeNo();
            if (isOrderACartTrade(orderInfo)) {
                outTradeNo = cartService.getCartTradeNo(orderInfo.getCartTrade());
            }
            AlipayTradeQueryResponse tradeQueryRes = alipayTools.tradeQuery(outTradeNo);
            if (tradeQueryRes.isSuccess()) {
                if (Objects.equals(tradeQueryRes.getTradeStatus(), "TRADE_SUCCESS")) {
                    if (isOrderACartTrade(orderInfo)) {
                        cartService.paySucceed(outTradeNo, Double.valueOf(tradeQueryRes.getTotalAmount()), tradeQueryRes.getTradeNo());
                    } else {
                        paySucceed(orderInfo, tradeQueryRes.getTradeNo());
                    }
                }
                return TradeStatus.get(tradeQueryRes.getTradeStatus());
            } else {
                return TradeStatus.TRADE_NOT_EXIST; // 不存在的订单号，或获取支付链接后未付款
            }
        }
        return TradeStatus.TRADE_SUCCESS;
    }

    @Override
    public TradeStatus payStatus(String tradeNo) {
        return payStatus(getOrder(tradeNo));
    }

    /**
     * 将订单计算价格、绑定优惠券、变为TOPAY态
     *
     * @param orderInfo
     * @param couponGroupId
     * @param user
     * @return
     */
    @Override
    public OrderInfo payOrder(OrderInfo orderInfo, String couponGroupId, User user) {
        if (couponGroupId == null) {
            orderInfo.setTotalAmount(orderInfo.getTotalAmountOriginal());
        } else {
            // 处理优惠券
            OrderPriceCalculateVO orderPriceInfo = new OrderPriceCalculateVO(orderInfo.getSku(), orderInfo.getCount(), couponGroupId);
            calculatePrice(false, orderPriceInfo, user);
            couponService.useCoupon(orderPriceInfo.getUsingCouponPO(), orderInfo);
            orderInfo.setTotalAmount(orderPriceInfo.getTotal_amount());
            orderInfo.setTotalAmountOriginal(orderPriceInfo.getTotal_amount_original());
        }
        orderInfo.setOrderStatus(OrderStatus.TOPAY);
        orderRepository.save(orderInfo);
        return orderInfo;
    }

    @Override
    public String pay(String tradeNo, String couponGroupId) {
        User user = securityUtil.getCurrentUser();
        OrderInfo orderInfo = getOrder(tradeNo);
        if (orderInfo.getOrderStatus() != OrderStatus.UNPAID && orderInfo.getOrderStatus() != OrderStatus.TOPAY)
            throw new BlueWhaleException("非可支付订单");
        if (isOrderACartTrade(orderInfo)) {
            // 用户选择了单独支付这个订单，使其所属的购物车订单失效
            cartService.disableCartTrade(orderInfo.getCartTrade());
        }
        if (orderInfo.getOrderStatus() == OrderStatus.TOPAY) {
            return alipayTools.getPayForm(orderInfo.getTradeNo(), orderInfo.getItemName(), orderInfo.getTotalAmount());
        }
        orderInfo = payOrder(orderInfo, couponGroupId, user);
        return alipayTools.getPayForm(orderInfo.getTradeNo(), orderInfo.getItemName(), orderInfo.getTotalAmount());
    }

    @Override
    public void paySucceed(OrderInfo orderInfo, String aliTradeNo) {
        orderInfo.setOrderStatus(OrderStatus.UNSEND);
        orderInfo.setTradeNoAli(aliTradeNo);
        orderRepository.save(orderInfo);
    }

    @Override
    public void paySucceed(String tradeNo, Double totalAmount, String aliTradeNo) {
        OrderInfo orderInfo = orderRepository.findByTradeNo(tradeNo);
        if (orderInfo == null) {
            alipayTools.refund(aliTradeNo, totalAmount);
            throw new BlueWhaleException("订单不存在");
        }
        if (!Objects.equals(totalAmount, orderInfo.getTotalAmount())) {
            alipayTools.refund(aliTradeNo, totalAmount);
            throw new BlueWhaleException("付款金额异常");
        }
        if (orderInfo.getOrderStatus() != OrderStatus.TOPAY) {
            alipayTools.refund(aliTradeNo, totalAmount);
            throw new BlueWhaleException("非可支付订单");
        }
        paySucceed(orderInfo, aliTradeNo);
    }
}
