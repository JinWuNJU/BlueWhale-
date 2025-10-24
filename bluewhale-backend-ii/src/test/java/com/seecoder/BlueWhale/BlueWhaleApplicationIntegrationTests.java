package com.seecoder.BlueWhale;

import com.seecoder.BlueWhale.configure.AlipayTools;
import com.seecoder.BlueWhale.enums.*;
import com.seecoder.BlueWhale.po.*;
import com.seecoder.BlueWhale.repository.*;
import com.seecoder.BlueWhale.service.*;
import com.seecoder.BlueWhale.serviceImpl.couponStrategy.Context;
import com.seecoder.BlueWhale.util.RandomUtil;
import com.seecoder.BlueWhale.util.SecurityUtil;
import com.seecoder.BlueWhale.vo.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class BlueWhaleApplicationIntegrationTests {

	@Autowired
	ImageInfoRepository imageInfoRepository;

	@Autowired
	UserRepository userRepository;
	@Autowired
	CouponService couponService;
	@Autowired
	CouponRepository couponRepository;
	@Autowired
	CouponGroupRepository couponGroupRepository;
	@Autowired
	StoreRepository storeRepository;
	@Autowired
	SkuRepository skuRepository;
	@Autowired
	AlipayTools alipayTools;
	@Autowired
	RandomUtil randomUtil;
	@Autowired
	ItemRepository itemRepository;
	@Autowired
	CartService cartService;
	@Autowired
	ItemService itemService;
	@Autowired
	OrderService orderService;
	@Autowired
	AddressRepository addressRepository;
	@Autowired
	OrderRepository orderRepository;
	@Autowired
	StoreService storeService;
	@Autowired
	UserService userService;
	@MockBean
	private SecurityUtil securityUtil;

	private User mockUser;

	Sku sku;

	//直接在现有数据库中查找

	Store mockStore;

	Item mockItem1;
	Item mockItem2;


	@BeforeEach
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void setup() {
		mockUser = new User();  // 创建一个测试用户
		// 假设设置一些必要的属性
		mockUser.setRole(RoleEnum.CUSTOMER);
		userRepository.save(mockUser);

		Address address = new Address();
		address.setName("name");
		address.setAddress("address ");
		address.setPhone("123456789");
		address.setUser(mockUser);
		mockUser.setDefaultAddress(address);
		addressRepository.save(address);
		userRepository.save(mockUser);

		when(securityUtil.getCurrentUser()).thenReturn(mockUser);  // 在每个测试之前配置返回一个非空的User对象


		//防止空指针
		ImageInfo info = imageInfoRepository.findRandomImageInfo();//使用原生SQL返回数据库中的一个随机imageInfo
		Set<ImageInfo> imageInfos = new HashSet<>();
		imageInfos.add(info);
		mockStore = new Store();
		mockStore.setLogoImageInfo(info);
		storeRepository.save(mockStore);


		mockItem1 = new Item();
		mockItem1.setStore(mockStore);
		mockItem1.setItemPrice("15");
		mockItem1.setItemCategory(ItemCategory.ELECTRONICS);
		mockItem1.setItemName("computer");
		mockItem1.setItemImage(imageInfos);

		sku = new Sku();
		sku.setItem(mockItem1);
		sku.setSkuInventory(1L);
		sku.setSkuPrice("11");
		sku.setSkuName("123");
		sku.setSkuLimitPerCustomer(10L);
		skuRepository.save(sku);

		mockItem2 = new Item();
		mockItem2.setStore(mockStore);
		mockItem2.setItemPrice("35");
		mockItem2.setItemCategory(ItemCategory.ELECTRONICS);
		mockItem2.setItemName("earphone");
		mockItem2.setItemImage(imageInfos);
		itemRepository.save(mockItem1);
		itemRepository.save(mockItem2);

	}

	@AfterEach
	public void clean() {
		Address address = mockUser.getDefaultAddress();
		mockUser.getDefaultAddress().setUser(null);
		mockUser.setDefaultAddress(null);
		userRepository.save(mockUser);
		addressRepository.delete(address);


		userRepository.delete(mockUser);
		itemRepository.delete(mockItem1);
		skuRepository.delete(sku);
		itemRepository.delete(mockItem2);
		storeRepository.delete(mockStore);
	}


	@Test
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void testAddressOperations() {
		AddressVO addressVO = new AddressVO();
		addressVO.setPhone("111111111");
		addressVO.setName("testAddress");
		addressVO.setName("user");
		Boolean addressAdded = userService.addAddress(addressVO);
		assertTrue(addressAdded);


		AddressListVO addressList = userService.getAddressList();
		assertFalse(addressList.getAddressList().isEmpty());

		int size = addressList.getAddressList().size();
		Address address = addressRepository.findById(addressList.getAddressList().get(size - 1).getAddressId()).get();
		long addrId = address.getId();

		addressVO.setName("Jane Doe");
		Boolean addressUpdated = userService.updateAddress(addrId, addressVO);
		assertTrue(addressUpdated);

		Boolean setDefaultAddress = userService.setDefaultAddress(addrId);
		assertTrue(setDefaultAddress);

		Throwable exception = assertThrows(Exception.class, () -> userService.deleteAddress(addrId));
		assertNotNull(exception);

		AddressListVO updatedAddressList = userService.getAddressList();
		assertFalse(updatedAddressList.getAddressList().stream()
				.noneMatch(addr -> addr.getAddressId().equals(addrId)));
	}

	@Test
	@Transactional
	void testAllStoreOperations() {
		StoreVO newStore = new StoreVO();
		newStore.setStoreName("My Awesome Store");
		newStore.setLogoUrl(imageInfoRepository.findRandomImageInfo().getUrl());
		Boolean created = storeService.create(newStore);
		assertTrue(created);

		// List stores
		StoreListVO storeList = storeService.list(0, ListSortType.time, SortingOrder.desc);
		assertFalse(storeList.getStore().isEmpty());

		//得到newStore的信息
		int size = storeList.getStore().size();
		StoreVO storeInfo = storeService.info(storeList.getStore().get(size - 1).getStoreId());
		assertNotNull(storeInfo);

		// 删除newStore
		StoreIdVO storeIdVO = new StoreIdVO();
		storeIdVO.setStoreIds(new String[]{storeInfo.getStoreId().toString()});
		Boolean removed = storeService.remove(storeIdVO);
		assertTrue(removed);
	}
	@Test
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	void testOrderLifeCycle() {
		OrderPriceCalculateVO orderPriceCalculateVO = new OrderPriceCalculateVO();
		orderPriceCalculateVO.setSkuId(sku.getSkuId()); // 假设1L对应的SKU已经在数据库中准备好
		orderPriceCalculateVO.setCount(1);
		orderPriceCalculateVO.setShipping(Shipping.DELIVERY);
		orderPriceCalculateVO.setAddressId(mockUser.getDefaultAddress().getId()); // 假设1L的地址信息已准备好
		OrderVO result = orderService.create(orderPriceCalculateVO);

		assertNotNull(result);
		// 验证返回的OrderVO对象的某些属性
		assertEquals(1, result.getCount());
		assertEquals(Shipping.DELIVERY, result.getShipping());
		assertNotNull(orderRepository.findByTradeNo(result.getOrderId()));

		OrderVO order = orderService.get(result.getOrderId());
		assertEquals(OrderStatus.UNPAID, order.getStatus());

		// 取消订单
		assertTrue(orderService.cancel(result.getOrderId()));

		// 重新获取订单以验证状态
		OrderInfo cancelledOrder = orderRepository.findByTradeNo(result.getOrderId());
		assertEquals(OrderStatus.CANCEL, cancelledOrder.getOrderStatus());
	}

	@Test
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	void testAddAndRemoveCartItem() {
		CartItemVO cartItemVO = new CartItemVO();
		cartItemVO.setSkuId(sku.getSkuId());
		cartItemVO.setCount(1);

		// 添加购物车项
		Boolean addResult = cartService.add(cartItemVO);
		assertTrue(addResult);

		// 验证购物车中有项目
		Set<CartItemVO> cartItems = cartService.getList();
		assertFalse(cartItems.isEmpty());

		// 移除购物车项
		CartItemVO toBeRemoved = cartItems.iterator().next();
		CartItemListVO cartItemListVO = new CartItemListVO();
		List<Long> idsToRemove = new ArrayList<>();
		idsToRemove.add(toBeRemoved.getId());
		cartItemListVO.setId(idsToRemove);
		Boolean removeResult = cartService.remove(cartItemListVO);
		assertTrue(removeResult);

		// 验证购物车已清空
		cartItems = cartService.getList();
		assertTrue(cartItems.isEmpty());
	}


	//由于单独mock storeRespository时，无法临时存储，还是沿用先前的方法，直接在数据库添加，再删掉
	@Test
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	void testListMethod() {
		couponRepository.save(new Coupon());

		List<Item> items = Arrays.asList(
				mockItem1, mockItem2
		);
		Integer storeId = mockStore.getStoreId();
		int page = 0;
		ListSortType sort = ListSortType.price;
		SortingOrder order = SortingOrder.asc;
		Double minPrice = 10.0;
		Double maxPrice = 50.0;
		ItemCategory[] itemCategory = {ItemCategory.ELECTRONICS};
		String keyword = "apple";


		// 调用 list 方法
		ItemListVO itemListVO = itemService.list(storeId, page, sort, order, minPrice, maxPrice, itemCategory, keyword);
		// 验证返回结果是否符合预期
		assertEquals(itemListVO, new ItemListVO(Collections.emptyList(), 0));

		//未加关键词查找
		itemListVO = itemService.list(storeId, page, sort, order, minPrice, maxPrice, itemCategory, null);
		assertEquals(itemListVO.item.size(), 2);
		assertEquals(itemListVO.totalPage, 1);
		assertEquals(itemListVO.item.get(0), mockItem1.toItemBasicVO());
		assertEquals(itemListVO.item.get(1), mockItem2.toItemBasicVO());
	}


	@Test
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	void pay() {
		System.out.println(alipayTools.getPayForm(randomUtil.nextString(18), "测试订单", 100D));
	}

	@Test
	@Transactional(propagation = Propagation.REQUIRES_NEW)
//确保存在一个活动的事务，并且 EntityManager 可以正确地管理实体状态的刷新。
	void testUseCouponUpdatesCountsCorrectly() {
		mockUser.setRole(RoleEnum.CEO);

		int totalCount = 100;
		String couponName = "测试优惠券";
		double thresholdAmount = 100;
		double discountAmount = 15;
		CouponGroupVO couponGroupVO = new CouponGroupVO();
		couponGroupVO.setCouponType(CouponType.FULL_REDUCTION);
		couponGroupVO.setCouponName(couponName);
		couponGroupVO.setTotalCount(totalCount);
		couponGroupVO.setThresholdAmount(thresholdAmount);
		couponGroupVO.setDiscountAmount(discountAmount);

		// 测试优惠券组创建服务
		couponGroupVO = couponService.createGroup(couponGroupVO);
		CouponGroup couponGroup = couponGroupRepository.findByCouponGroupId(couponGroupVO.getCouponGroupId());
		assertEquals(totalCount, couponGroup.getRemainingCount());
		assertEquals(totalCount, couponGroup.getTotalCount());
		assertEquals(couponName, couponGroup.getCouponName());
		assertEquals(thresholdAmount, couponGroup.getThresholdAmount());
		assertEquals(discountAmount, couponGroup.getDiscountAmount());

		// 测试领取优惠券服务
		couponService.claim(couponGroupVO.getCouponGroupId());
		// 领取优惠券后,totalCount = remainingCount-1
		assertEquals(totalCount - 1, couponGroup.getRemainingCount());

		// 测试价格计算
		Context context = Context.getContext(couponGroupVO);
		assertFalse(context.valid(thresholdAmount - 1));
		assertTrue(context.valid(thresholdAmount));
		assertEquals(thresholdAmount - discountAmount, context.calculate(thresholdAmount));

		// 测试使用优惠券服务
		Coupon coupon = new Coupon();
		coupon.setCouponGroup(couponGroup);
		couponService.useCoupon(coupon, orderRepository.findAll().get(0));//orderinfo默认从仓库中选一个
		//useCoupon后,usedCount+1
		couponGroup.setStore(storeRepository.findByStoreId(1));
		assertEquals(1, couponGroup.getUsedCount());

		mockUser.setRole(RoleEnum.CUSTOMER);
		couponRepository.delete(coupon);
		couponGroupRepository.delete(couponGroup);
	}


}
