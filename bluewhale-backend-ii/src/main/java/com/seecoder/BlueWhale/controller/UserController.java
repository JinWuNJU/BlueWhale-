package com.seecoder.BlueWhale.controller;

import com.seecoder.BlueWhale.service.UserService;
import com.seecoder.BlueWhale.vo.AddressListVO;
import com.seecoder.BlueWhale.vo.AddressVO;
import com.seecoder.BlueWhale.vo.ResultVO;
import com.seecoder.BlueWhale.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    UserService userService;


    @PostMapping("/register")
    public ResultVO<Boolean> register(@RequestBody @Valid UserVO userVO){
        return ResultVO.buildSuccess(userService.register(userVO));
    }


    @PostMapping("/address")
    public ResultVO<Boolean> addAddress(@RequestBody @Valid AddressVO addressVO){
        return ResultVO.buildSuccess(userService.addAddress(addressVO));
    }

    @PostMapping("/login")
    public ResultVO<String> login(@RequestParam("phone") String phone, @RequestParam("password") String password){
        return ResultVO.buildSuccess(userService.login(phone, password));
    }

    @GetMapping("/address")
    public ResultVO<AddressListVO> info(){
        return ResultVO.buildSuccess(userService.getAddressList());
    }

    @DeleteMapping("/address/{addressId}")
    public ResultVO<Boolean> deleteAddress(@PathVariable(name = "addressId") Long addressId){
        return ResultVO.buildSuccess(userService.deleteAddress(addressId));
    }

    @PutMapping("/address/{addressId}")
    public ResultVO<Boolean> updateAddress(@PathVariable(name = "addressId") Long addressId,
                                           @RequestBody @Valid AddressVO addressVO){
        return ResultVO.buildSuccess(userService.updateAddress(addressId,addressVO));
    }

    @PostMapping("/address/{addressId}")
    public ResultVO<Boolean> setDefaultAddress(@PathVariable(name = "addressId") Long addressId){
        return ResultVO.buildSuccess(userService.setDefaultAddress(addressId));
    }

    @GetMapping
    public ResultVO<UserVO> getInformation(){
        return ResultVO.buildSuccess(userService.getInformation());
    }

    @PostMapping
    public ResultVO<Boolean> updateInformation(@RequestBody UserVO userVO){
        return ResultVO.buildSuccess(userService.updateInformation(userVO));
    }
}
