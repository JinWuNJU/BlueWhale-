package com.seecoder.BlueWhale.service;

import com.seecoder.BlueWhale.po.Address;
import com.seecoder.BlueWhale.po.User;
import com.seecoder.BlueWhale.vo.AddressListVO;
import com.seecoder.BlueWhale.vo.AddressVO;
import com.seecoder.BlueWhale.vo.UserVO;

public interface UserService {
    Boolean register(UserVO userVO);

    String login(String phone,String password);

    UserVO getInformation();

    Boolean addAddress(AddressVO addressVO);

    Boolean updateInformation(UserVO userVO);

    Address getAddress(Long addressId, User user);

    Boolean deleteAddress(Long addressId);

    Boolean updateAddress(Long addressId ,AddressVO addressVO);

    Boolean setDefaultAddress(Long addressId);

    AddressListVO getAddressList();
}
