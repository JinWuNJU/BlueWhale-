package com.seecoder.BlueWhale.configure;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.seecoder.BlueWhale.exception.BlueWhaleException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@ConfigurationProperties(prefix = "alipay")
@Component
@Getter
@Setter
public class AlipayTools {

    private String serverUrl;
    private String appId;
    private String appPrivateKey;
    private String alipayPublicKey;
    private String notifyUrl;
    private String returnUrl;
    private static String format = "json";
    private static String charset = "utf-8";
    private static String signType = "RSA2";

    private AlipayClient getAliPayClient() {
        return new DefaultAlipayClient(serverUrl, appId, appPrivateKey, format, charset, alipayPublicKey, signType);
    }

    /**
     * @Author: DingXiaoyu
     * @Date: 11:25 2024/1/31
     * 使用支付宝沙箱
     * 使用时可以根据自己的需要做修改，包括参数名、返回值、具体实现
     * 在bizContent中放入关键的信息：outTradeNo、price、name
     * 返回的form是一个String类型的html页面
     */
    public String getPayForm(String outTradeNo, String name, Double price) {
        AlipayClient alipayClient = getAliPayClient();
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(outTradeNo);
        model.setTotalAmount(String.valueOf(price));
        model.setSubject(name);
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        model.setTimeoutExpress("10m");
        request.setBizModel(model);
        String form;
        try {
            form = alipayClient.pageExecute(request).getBody();
        } catch (Exception e) {
            throw new BlueWhaleException("支付出错");
        }
        return form;
    }

    public AlipayTradeQueryResponse tradeQuery(String outTradeNo) {
        AlipayClient alipayClient = getAliPayClient();
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(outTradeNo);
        request.setBizModel(model);
        try {
            return alipayClient.execute(request);
        } catch (Exception e) {
            throw new BlueWhaleException("查询交易出错");
        }
    }

    public AlipayTradeRefundResponse refund(String tradeNoAli, Double refundAmount) {
        AlipayClient alipayClient = getAliPayClient();
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setTradeNo(tradeNoAli);
        model.setRefundAmount(String.valueOf(refundAmount));
        request.setBizModel(model);
        try {
            return alipayClient.execute(request);
        } catch (Exception e) {
            throw new BlueWhaleException("退款出错");
        }
    }

    public boolean rsaCheck(Map<String, String> paramsMap) {
        try {
            if (!paramsMap.get("app_id").equals(appId))
                return false;
            return AlipaySignature.rsaCheckV1(paramsMap, alipayPublicKey, charset, signType);
        } catch (Exception e) {
            return false;
        }
    }
}