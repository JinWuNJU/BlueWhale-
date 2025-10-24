package com.seecoder.BlueWhale.serviceImpl;

import com.seecoder.BlueWhale.aspect.AuthLevelCheck;
import com.seecoder.BlueWhale.configure.AlipayTools;
import com.seecoder.BlueWhale.enums.CartSkuStatus;
import com.seecoder.BlueWhale.enums.TradeStatus;
import com.seecoder.BlueWhale.exception.BlueWhaleException;
import com.seecoder.BlueWhale.po.*;
import com.seecoder.BlueWhale.repository.CartRepository;
import com.seecoder.BlueWhale.repository.CartTradeRepository;
import com.seecoder.BlueWhale.service.*;
import com.seecoder.BlueWhale.util.RandomUtil;
import com.seecoder.BlueWhale.util.SecurityUtil;
import com.seecoder.BlueWhale.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    private static final int CartItemLimit = 50;
    @Autowired
    SecurityUtil securityUtil;
    @Autowired
    ItemService itemService;
    @Autowired
    CartRepository cartRepository;
    @Autowired
    CartTradeRepository cartTradeRepository;
    @Autowired
    OrderService orderService;
    @Autowired
    RandomUtil randomUtil;
    @Autowired
    AlipayTools alipayTools;
    @Autowired
    CouponService couponService;

    /**
     * 添加购物车项到购物车中
     * 
     * @param cartItemVO 购物车项
     * @return 移除是否成功
     */
    @Override
    @AuthLevelCheck
    public Boolean add(CartItemVO cartItemVO) {
        User user = securityUtil.getCurrentUser();
        Set<Cart> carts = cartRepository.findByUser(user);
        if (carts.size() == CartItemLimit)
            throw new BlueWhaleException("超出购物车上限");
        Sku sku = itemService.getSkuById(cartItemVO.getSkuId());
        Cart cart = cartItemVO.toPO(user, sku);
        cartRepository.save(cart);
        return true;
    }

    /**
     * 从购物车中移除一组购物车项
     * @param cartItemSet 购物车项id组
     * @return 移除是否成功
     */
    @Override
    @AuthLevelCheck
    public Boolean remove(CartItemListVO cartItemSet) {
        User user = securityUtil.getCurrentUser();
        cartRepository.deleteAll(getCart(cartItemSet.getId(), user));
        return true;
    }

    /**
     * 获取购物车项，确保购物车项属于当前用户
     * @param user 用户
     * @param cartItemId 购物车项id
     */
    private Cart getCart(User user, Long cartItemId) {
        Optional<Cart> cartOptional = cartRepository.findById(cartItemId);
        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();
            if (!Objects.equals(cart.getUser().getId(), user.getId())) {
                throw BlueWhaleException.illegalParameter();
            }
            return cart;
        } else {
            throw BlueWhaleException.illegalParameter();
        }
    }

    /**
     * 获取购物车列表，转化为VO向前端提供
     */
    @Override
    @AuthLevelCheck
    public Set<CartItemVO> getList() {
        User user = securityUtil.getCurrentUser();
        return cartRepository.findByUser(user).stream()
                .map(v -> v.toVO(orderService))
                .collect(Collectors.toSet());
    }

    /**
     * 修改一个购物车项的数量
     * @param cartItemVO 购物车项
     */
    @Override
    @AuthLevelCheck
    public CartItemVO put(CartItemVO cartItemVO) {
        User user = securityUtil.getCurrentUser();
        Cart cart = getCart(user, cartItemVO.getId());
        if (cartItemVO.getCount() != null)
            cart.setCount(cartItemVO.getCount());
        cartRepository.save(cart);
        return cart.toVO(orderService);
    }

    /**
     * 当sku库存不足时，将购物车中的sku状态设置为缺货，由ItemService负责调用
     */
    @Override
    public void skuShortage(Sku sku) {
        Set<Cart> carts = sku.getCarts();
        if(carts == null || carts.isEmpty()){
            return;
        }
        carts.forEach(v -> v.setSkuStatus(CartSkuStatus.SHORTAGE));
        cartRepository.saveAll(carts);
    }

    /**
     * 当sku库存恢复时，将购物车中的sku状态设置为正常，由ItemService负责调用
     */
    @Override
    public void skuRestock(Sku sku) {
        Set<Cart> carts = sku.getCarts();
        if(carts == null || carts.isEmpty()){
            return;
        }
        carts.forEach(v -> v.setSkuStatus(CartSkuStatus.NORMAL));
        cartRepository.saveAll(carts);
    }

    /**
     * 获取一组购物车项，可确保结果的顺序与输入的购物车项id顺序一致
     * @param id 购物车id组
     * @param user 用户
     * @return 购物车列表
     */
    List<Cart> getCart(List<Long> id, User user) {
        List<Cart> carts = cartRepository.findByUserAndIdIsIn(user, id);
        HashMap<Long, Cart> cartMap = new HashMap<>(); // 确保结果和输入的id顺序一致
        carts.forEach(v -> cartMap.put(v.getId(), v));
        ArrayList<Cart> cartList = new ArrayList<>();
        for (Long i : id) {
            if (cartMap.containsKey(i)) {
                cartList.add(cartMap.get(i));
            }
        }
        return cartList;
    }

    private static final String cartTradeNoPrefix = "cart";

    private String generateCartTradeNo() {
        return randomUtil.generateUniqueId(v -> cartTradeRepository.findByTradeNo(v) != null);
    }

    private String generateCartTradeName(CartTrade cartTrade) {
        return "购物车订单 - 共" + cartTrade.getOrders().size() + "组商品";
    }

    /**
     * 判断交易号是否为购物车交易号
     * @param tradeNo 交易号
     * @return 是否为购物车交易号
     */
    @Override
    public boolean isCartTradeNo(String tradeNo) {
        if (tradeNo.startsWith(cartTradeNoPrefix))
            return true;
        return false;
    }

    /**
     * 创建购物车交易 <p>
     * 创建一个CartTrade，使用OrderService对应创建订单，将订单与购物车交易关联 <p>
     * 创建成功后删除购物车中的购物车项，CartTrade的tradeStatus为TRADE_NOT_EXIST（代表未创建支付宝交易） <p>
     * 
     * @param cartTradeCreate 购物车交易创建信息
     * @return 购物车交易号
     */
    @Override
    @Transactional
    public String createTrade(CartTradeCreateVO cartTradeCreate) {
        if (cartTradeCreate.getId() == null || cartTradeCreate.getId().isEmpty())
            throw BlueWhaleException.illegalParameter();
        User user = securityUtil.getCurrentUser();
        List<Cart> carts = getCart(cartTradeCreate.getId(), user);
        if (carts.isEmpty())
            throw BlueWhaleException.illegalParameter();
        for (Cart c : carts) {
            if (c.getSkuStatus() == CartSkuStatus.EXPIRED)
                throw new BlueWhaleException(String.format("商品 %s （%s） 已失效", c.getItemName(), c.getSkuName()));
            if (c.getSkuStatus() == CartSkuStatus.SHORTAGE)
                throw new BlueWhaleException(String.format("商品 %s （%s） 已无货", c.getItemName(), c.getSkuName()));
        }
        ArrayList<OrderInfo> orders = new ArrayList<>();
        double totalAmount = 0;
        CartTrade cartTrade = new CartTrade();
        cartTrade.setTradeNo(generateCartTradeNo());
        cartTradeRepository.save(cartTrade);
        for (Cart c : carts) {
            Sku s = c.getSku();
            OrderInfo o = orderService.create(c.getCount(), user, s, cartTradeCreate.getAddressId(), cartTradeCreate.getShipping()); //TODO
            orders.add(o);
            totalAmount += o.getTotalAmount();
        }
        cartTrade.setTotalAmount(totalAmount);
        cartTrade.setTradeStatus(TradeStatus.TRADE_NOT_EXIST);
        cartTrade.setOrders(orders);
        cartTrade.setUser(user);
        cartTradeRepository.save(cartTrade);
        cartRepository.deleteAll(carts);
        return cartTrade.getTradeNo();
    }
    
    /**
     * 获取购物车交易支付链接 <p>
     * 成功后CartTrade的tradeStatus为WAIT_BUYER_PAY <p>
     * 
     * @param tradeNo 购物车交易号
     * @param usingCoupon 使用的优惠券
     * @return 支付链接
     */
    @Override
    @Transactional
    public String pay(String tradeNo, Set<CartTradeCouponVO> usingCoupon) {
        User user = securityUtil.getCurrentUser();
        CartTrade cartTrade = getCartTrade(tradeNo, user);
        if (cartTrade.getTradeStatus() == TradeStatus.WAIT_BUYER_PAY)
            return alipayTools.getPayForm(cartTrade.getTradeNo(), generateCartTradeName(cartTrade), cartTrade.getTotalAmount());
        // 检查是否有重复优惠券
        if (usingCoupon == null)
            usingCoupon = new HashSet<>();
        HashSet<String> coupons = new HashSet<>();
        for (CartTradeCouponVO c : usingCoupon) {
            if (coupons.contains(c.getCouponGroupId()))
                throw new BlueWhaleException("重复的优惠券");
            coupons.add(c.getCouponGroupId());
        }
        List<OrderInfo> orders = cartTrade.getOrders();
        double totalAmount = 0;
        for (OrderInfo o : orders) {
            String couponGroup = null;
            for (CartTradeCouponVO c : usingCoupon) {
                if (c.getOrderTradeNo().equals(o.getTradeNo())) {
                    couponGroup = c.getCouponGroupId();
                    usingCoupon.remove(c);
                    break;
                }
            }
            o = orderService.payOrder(o, couponGroup, user);
            totalAmount += o.getTotalAmount();
        }

        cartTrade.setTotalAmount(totalAmount);
        cartTrade.setTradeStatus(TradeStatus.WAIT_BUYER_PAY);
        cartTradeRepository.save(cartTrade);
        return alipayTools.getPayForm(cartTrade.getTradeNo(), generateCartTradeName(cartTrade), cartTrade.getTotalAmount());
    }


    /**
     * 当sku被删除时，将购物车中的sku状态设置为失效，由ItemService负责调用
     */
    @Override
    public void skuRemoval(Sku sku) {
        if(sku == null){
            return;
        }
        sku.getCarts().forEach(v -> {
            v.setSkuStatus(CartSkuStatus.EXPIRED);
            v.setSku(null);
        });
        sku.setCarts(null);
    }

    /**
     * 禁用购物车交易，用于取消订单、单独支付订单的情况，由OrderService调用
     */
    @Override
    public void disableCartTrade(CartTrade cartTrade) {
        cartTrade.setTradeStatus(TradeStatus.TRADE_CLOSED);
        cartTrade.setOrders(new ArrayList<>());
        cartTradeRepository.save(cartTrade);
    }

    /**
     * 支付成功回调，由AlipayController调用 <p>
     * 正常情况下，支付成功CartTrade的tradeStatus为TRADE_SUCCESS <p>
     * 若出现异常，将支付宝交易退款 <p>
     */
    @Override
    public void paySucceed(String tradeNo, Double totalAmount, String aliTradeNo) {
        CartTrade cartTrade = cartTradeRepository.findByTradeNo(tradeNo);
        if (cartTrade == null) {
            alipayTools.refund(aliTradeNo, totalAmount);
            throw new BlueWhaleException("订单不存在");
        }
        if (!Objects.equals(totalAmount, cartTrade.getTotalAmount())) {
            alipayTools.refund(aliTradeNo, totalAmount);
            throw new BlueWhaleException("付款金额异常");
        }
        if (cartTrade.getTradeStatus() != TradeStatus.WAIT_BUYER_PAY) {
            alipayTools.refund(aliTradeNo, totalAmount); // 可能由于单独支付购物车的某一订单后又将整个购物车支付
            throw new BlueWhaleException("非可支付订单");
        }
        cartTrade.setTradeNoAli(aliTradeNo);
        cartTrade.getOrders().forEach(v -> orderService.paySucceed(v, aliTradeNo));
        cartTrade.setTradeStatus(TradeStatus.TRADE_SUCCESS);
        cartTradeRepository.save(cartTrade);
    }

    /**
     * 获取CartTrade交易号
     */
    @Override
    public String getCartTradeNo(CartTrade cartTrade) {
        return cartTrade.getTradeNo();
    }

    /**
     * 结算购物车，计算购物车中的商品总价格，与商品可用的优惠券信息 <p>
     * 同时计算使用优惠券后的最低总价格 <p>
     */
    @Override
    public CartTradeCalculateVO calculate(CartItemListVO cartItemListVO) {
        if (cartItemListVO.getId() == null || cartItemListVO.getId().isEmpty())
            throw BlueWhaleException.illegalParameter();
        User user = securityUtil.getCurrentUser();
        List<Cart> carts = getCart(cartItemListVO.getId(), user);
        if (carts.isEmpty())
            throw BlueWhaleException.illegalParameter();
        ArrayList<OrderPriceCalculateVO> orderPrices = new ArrayList<>();
        for (Cart c : carts) {
            if (c.getSkuStatus() == CartSkuStatus.EXPIRED)
                throw new BlueWhaleException(String.format("商品 %s （%s） 已失效", c.getItemName(), c.getSkuName()));
            if (c.getSkuStatus() == CartSkuStatus.SHORTAGE)
                throw new BlueWhaleException(String.format("商品 %s （%s） 已无货", c.getItemName(), c.getSkuName()));
            orderPrices.add(new OrderPriceCalculateVO(c.getSku(), c.getCount(), null));
        }
        return calculate(orderPrices, user);
    }

    private CartTradeCalculateVO calculate(ArrayList<OrderPriceCalculateVO> orderPrices, User user) {
        CartTradeCalculateVO calculate = new CartTradeCalculateVO();
        couponService.getCouponListForOrders(orderPrices, user);
        calculate.setOrderPrice(orderPrices);
        double total_original = 0, total_discounted = 0, total = 0;
        for (OrderPriceCalculateVO o : orderPrices) {
            total_discounted += o.getTotal_amount_max_discount();
            total_original += o.getTotal_amount_original();
            total += o.getTotal_amount();
        }
        calculate.setTotal_amount(total);
        calculate.setTotal_amount_original(total_original);
        calculate.setTotal_amount_max_discount(total_discounted);
        return calculate;
    }

    private CartTrade getCartTrade(String tradeNo, User user) {
        CartTrade cartTrade = cartTradeRepository.findByTradeNoAndUser(tradeNo, user);
        if (cartTrade == null)
            throw new BlueWhaleException("交易不存在");
        return cartTrade;
    }

    @Override
    public CartTradeVO getTrade(String tradeNo, Set<CartTradeCouponVO> usingCoupon) {
        User user = securityUtil.getCurrentUser();
        CartTrade cartTrade = getCartTrade(tradeNo, user);
        if (cartTrade.getTradeStatus() != TradeStatus.TRADE_NOT_EXIST)
            throw new BlueWhaleException("非可查看交易");
        HashSet<String> coupons = new HashSet<>();
        for (CartTradeCouponVO c : usingCoupon) {
            if (c.getCouponGroupId() != null && c.getCouponGroupId().length() > 0) {
                if (coupons.contains(c.getCouponGroupId()))
                    throw new BlueWhaleException("重复的优惠券");
                coupons.add(c.getCouponGroupId());
            }
        }
        CartTradeVO cartTradeVO = new CartTradeVO();
        cartTradeVO.setTradeNo(cartTrade.getTradeNo());
        cartTradeVO.setTradeStatus(cartTrade.getTradeStatus());
        ArrayList<CartTradeOrderVO> trade = new ArrayList<>();
        ArrayList<OrderPriceCalculateVO> orderPrices = new ArrayList<>();
        for (OrderInfo o : cartTrade.getOrders()) {
            OrderPriceCalculateVO priceVO = new OrderPriceCalculateVO(o);
            for (CartTradeCouponVO coupon : usingCoupon) {
                if (coupon.getOrderTradeNo().equals(o.getTradeNo())) {
                    priceVO.setCouponGroupId(coupon.getCouponGroupId());
                }
            }
            orderPrices.add(priceVO);
            trade.add(new CartTradeOrderVO(o.toVO(), priceVO));
        }
        CartTradeCalculateVO calculateResult = calculate(orderPrices, user);
        cartTradeVO.setTrade(trade);
        cartTradeVO.setTotal_amount(calculateResult.getTotal_amount());
        cartTradeVO.setTotal_amount_original(calculateResult.getTotal_amount_original());
        return cartTradeVO;
    }
}
