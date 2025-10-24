package com.seecoder.BlueWhale.vo;

import com.seecoder.BlueWhale.po.ExpressInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExpressInfoVO {
    String mailNo;

    public ExpressInfo toPO() {
        ExpressInfo expressInfo = new ExpressInfo();
        expressInfo.setMailNo(getMailNo());
        return expressInfo;
    }
}
