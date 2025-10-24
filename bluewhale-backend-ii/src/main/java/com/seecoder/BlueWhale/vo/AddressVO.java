package com.seecoder.BlueWhale.vo;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Size;


@Getter
@Setter
@NoArgsConstructor
public class AddressVO {
	@Size(min=1, max=20)
	private String name;
	@Size(min=1, max=70)
	private String address;
	@Length(min=11, max=11)
	private String phone;
}
