package com.seecoder.BlueWhale.vo;

import com.seecoder.BlueWhale.enums.RoleEnum;
import com.seecoder.BlueWhale.po.Store;
import com.seecoder.BlueWhale.po.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class UserVO {

    private Integer id;
    @Size(min = 1, max = 10)
    private String name;
    @Size(min = 11, max = 11)
    private String phone;
    @Size(min = 1, max = 16)
    private String password;

    private Integer storeId;
    @Size(min = 1, max = 70)
    private String address;

    private RoleEnum role;

    private Date createTime;

    private String storeName;

    public User toPO(Store store){
        User user=new User();
        user.setId(this.id);
        user.setName(this.name);
        user.setPhone(this.phone);
        user.setRole(this.role);
        user.setStore(store);
        user.setPassword(this.password);
        user.setCreateTime(this.createTime);
        return user;
    }
}
