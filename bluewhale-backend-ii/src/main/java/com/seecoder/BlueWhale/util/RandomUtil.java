package com.seecoder.BlueWhale.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.function.Function;

@Component
public class RandomUtil {
    @Autowired
    SecureRandom secureRandom;

    public String nextString(int len) {
        byte[] bin = new byte[len];
        secureRandom.nextBytes(bin);
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encoded = encoder.encode(bin);
        return (new String(encoded)).replace('/', '_').replace('+', '-');
    }

    /**
     * 生成随机标识符，直到找到一个唯一的标识符，生成一个唯一的标识符。
     *
     * @param isExist 用于检查生成的标识符是否已存在的函数
     * @return urlbase64编码后的12字节随机值
     */
    public String generateUniqueId(Function<String, Boolean> isExist) {
        String randomId;
        do {
            randomId = nextString(12);
        } while (isExist.apply(randomId));
        return randomId;
    }
}
