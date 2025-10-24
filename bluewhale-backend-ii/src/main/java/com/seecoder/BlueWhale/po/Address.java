package com.seecoder.BlueWhale.po;

import com.seecoder.BlueWhale.vo.AddressVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "address")
public class Address {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;

	@Basic
	@Column(name = "address")
	private String address;

	@Basic
	@Column(name = "phone")
	private String phone;

	@Basic
	@Column(name = "name")
	private String name;

	public AddressVO toVO(){
		AddressVO addressvo = new AddressVO();
		addressvo.setAddress(this.getAddress());
		addressvo.setName(this.getName());
		addressvo.setPhone(this.getPhone());
		return addressvo;
	}

}
