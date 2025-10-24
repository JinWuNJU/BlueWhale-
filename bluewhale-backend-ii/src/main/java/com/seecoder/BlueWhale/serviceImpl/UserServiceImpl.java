package com.seecoder.BlueWhale.serviceImpl;

import com.seecoder.BlueWhale.exception.BlueWhaleException;
import com.seecoder.BlueWhale.po.Address;
import com.seecoder.BlueWhale.po.Store;
import com.seecoder.BlueWhale.po.User;
import com.seecoder.BlueWhale.repository.AddressRepository;
import com.seecoder.BlueWhale.repository.StoreRepository;
import com.seecoder.BlueWhale.repository.UserRepository;
import com.seecoder.BlueWhale.service.UserService;
import com.seecoder.BlueWhale.util.SecurityUtil;
import com.seecoder.BlueWhale.util.TokenUtil;
import com.seecoder.BlueWhale.vo.AddressIdVO;
import com.seecoder.BlueWhale.vo.AddressListVO;
import com.seecoder.BlueWhale.vo.AddressVO;
import com.seecoder.BlueWhale.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: GaoZhaolong
 * @Date: 14:46 2023/11/26
 *
 * 注册登录功能实现
 */
@Service
public class UserServiceImpl implements UserService {
	@Autowired
	AddressRepository addressRepository;

	@Autowired
	UserRepository userRepository;
	@Autowired
	StoreRepository storeRepository;

	@Autowired
	TokenUtil tokenUtil;

	@Autowired
	SecurityUtil securityUtil;


	@Override
	public Boolean register(UserVO userVO) {
		User user = userRepository.findByPhone(userVO.getPhone());
		if (user != null) {
			throw BlueWhaleException.phoneAlreadyExists();
		}
		Store store = null;
		if (userVO.getStoreId() != null) {
			store = storeRepository.findByStoreId(userVO.getStoreId());
		}
		User newUser = userVO.toPO(store);
		newUser.setCreateTime(new Date());
		userRepository.save(newUser);

		Address address = new Address();
		address.setAddress(userVO.getAddress());
		address.setName(userVO.getName());
		address.setPhone(userVO.getPhone());
		newUser.setDefaultAddress(address);

		newUser.getDefaultAddress().setUser(newUser);
		addressRepository.save(newUser.getDefaultAddress());
		return true;
	}

	@Override
	public String login(String phone, String password) {
		User user = userRepository.findByPhoneAndPassword(phone, password);
		if (user == null) {
			throw BlueWhaleException.phoneOrPasswordError();
		}
		return tokenUtil.getToken(user);
	}

	@Override
	public UserVO getInformation() {
		User user=securityUtil.getCurrentUser();
		UserVO userVO = user.toVO();
		if (user.getStore() != null) {
			userVO.setStoreName(user.getStore().getStoreName());
		}
		return userVO;
	}

	@Override
	public Boolean addAddress(AddressVO addressVO) {
		User user = securityUtil.getCurrentUser();
		if(addressRepository.findByUser(user).size() >= 10)
			throw new BlueWhaleException("最多可储存10个地址");
		Address address = new Address();
		address.setUser(user);
		address.setAddress(addressVO.getAddress());
		address.setPhone(addressVO.getPhone());
		address.setName(addressVO.getName());
		addressRepository.save(address);
		return true;
	}

	@Override
	public Boolean updateInformation(UserVO userVO) {
		User user=securityUtil.getCurrentUser();
		if (userVO.getPassword()!=null){
			user.setPassword(userVO.getPassword());
		}
		if (userVO.getName()!=null){
			user.setName(userVO.getName());
		}
		userRepository.save(user);
		return true;
	}

	@Override
	public Address getAddress(Long addressId, User user) {
		Optional<Address> address = addressRepository.findByIdAndUser(addressId, user);
		if(!address.isPresent()){
			throw new BlueWhaleException("该地址不存在!");
		}
		return address.get();
	}

	@Override
	public Boolean deleteAddress(Long addressId) {
		User user = securityUtil.getCurrentUser();
		Address address = getAddress(addressId, user);
		if(address.getId().equals(user.getDefaultAddress().getId())){
			throw new BlueWhaleException("不能删除默认地址!");
		}
		addressRepository.delete(address);
		return true;
	}

	@Override
	public Boolean updateAddress(Long addressId ,AddressVO addressVO) {
		User user = securityUtil.getCurrentUser();
		Address address = getAddress(addressId, user);
		address.setName(addressVO.getName());
		address.setPhone(addressVO.getPhone());
		address.setAddress(addressVO.getAddress());
		address.setUser(securityUtil.getCurrentUser());
		addressRepository.save(address);
		return true;
	}

	@Override
	public Boolean setDefaultAddress(Long addressId) {
		User user = securityUtil.getCurrentUser();
		Address address = getAddress(addressId, user);
		//改变用户的默认地址
		user.setDefaultAddress(address);
		userRepository.save(user);
		return true;
	}

	@Override
	public AddressListVO getAddressList() {
		User user = securityUtil.getCurrentUser();
		Set<Address> addresses = addressRepository.findByUser(user);
		return new AddressListVO(addresses.stream().map(this::toAddressIdVO).collect(Collectors.toList()), user.getDefaultAddress().getId());
	}

	private AddressIdVO toAddressIdVO(Address address){
		AddressIdVO addressIdVO = new AddressIdVO();
		addressIdVO.setAddress(address.getAddress());
		addressIdVO.setName(address.getName());
		addressIdVO.setPhone(address.getPhone());
		addressIdVO.setAddressId(address.getId());
		return addressIdVO;
	}

}
