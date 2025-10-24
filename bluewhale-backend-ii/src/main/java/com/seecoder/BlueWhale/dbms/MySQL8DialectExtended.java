package com.seecoder.BlueWhale.dbms;

import org.hibernate.dialect.MySQL8Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StandardBasicTypes;

/**
 * 向hibernate.MySQL8Dialect添加match against(自然语言全文搜索)的语法
 * 相比额外引入专用全文搜素引擎、mysql自带的方案效果还行，而且代码改动最少
 * @see https://pavelmakhov.com/2016/09/jpa-custom-function/
 */
public class MySQL8DialectExtended extends MySQL8Dialect {
    public MySQL8DialectExtended() {
        super();
        registerFunction("match", new SQLFunctionTemplate(StandardBasicTypes.DOUBLE,
                "match(?1) against (?2)"));
    }
}
