package com.seecoder.BlueWhale.po;

import com.seecoder.BlueWhale.enums.TradeStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cart_trade", indexes = {
})
public class CartTrade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "trade_no")
    private String tradeNo;
    @Column(name = "trade_no_ali")
    private String tradeNoAli;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "cart_trade_order",
            joinColumns = {@JoinColumn(name = "trade_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "order_id", referencedColumnName = "order_id")}
    )
    private List<OrderInfo> orders;
    @Column(name = "trade_status")
    @Enumerated(EnumType.STRING)
    private TradeStatus tradeStatus;
    @Column(name = "total_amount")
    private Double totalAmount;
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
}
