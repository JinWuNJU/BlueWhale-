package com.seecoder.BlueWhale.exception;

/**
 * @Author: DingXiaoyu
 * @Date: 0:26 2023/11/26
 * 你可以在这里自定义Exception
*/
public class BlueWhaleException extends RuntimeException{

    public BlueWhaleException(String message){
        super(message);
    }
    public static BlueWhaleException phoneAlreadyExists(){
        return new BlueWhaleException("手机号已经存在!");
    }

    public static BlueWhaleException notLogin(){
        return new BlueWhaleException("未登录!");
    }

    public static BlueWhaleException authLevelMismatch(){
        return new BlueWhaleException("账号权限不匹配!");
    }

    public static BlueWhaleException phoneOrPasswordError(){
        return new BlueWhaleException("手机号或密码错误!");
    }

    public static BlueWhaleException fileUploadFail(){
        return new BlueWhaleException("文件上传失败!");
    }

    public static BlueWhaleException storeIdNotExists(){
        return new BlueWhaleException("商店不存在!");
    }

    public static BlueWhaleException storeNameAlreadyExists(){
        return new BlueWhaleException("商店名已经存在!");
    }

    public static BlueWhaleException illegalParameter(){
        return new BlueWhaleException("非法参数!");
    }

    public static BlueWhaleException fileFormatUnknown() {
        return new BlueWhaleException("未知的文件格式");
    }

    public static BlueWhaleException imageExpired() {
        return new BlueWhaleException("图片已过期，请重新上传");
    }

    public static BlueWhaleException itemIdNotExists(){
        return new BlueWhaleException("商品不存在!");
    }
    public static BlueWhaleException skuIdNotExists(){
        return new BlueWhaleException("该库存不存在!");
    }

    public static BlueWhaleException creatingDuplicatedItem(){
        return new BlueWhaleException("重复创建商品");
    }
    public static BlueWhaleException itemIdNotInStore(){
        return new BlueWhaleException("该商品不属于此商店！");

    }




}
