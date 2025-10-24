package com.seecoder.BlueWhale.po;

import com.seecoder.BlueWhale.vo.ReviewVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(indexes = {
        @Index(columnList = "rating"),
        @Index(columnList = "review_time")
})
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private int reviewId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", referencedColumnName = "item_id")
    private Item item;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", referencedColumnName = "sku_id")
    private Sku sku;
    @OneToOne
    @JoinColumn(name = "order_id", referencedColumnName = "order_id")
    private OrderInfo orderInfo;
    @Column(name = "review_time")
    private Date reviewTime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    @Column(name = "comment_text", columnDefinition = "varchar(255) CHARACTER SET gb18030 COLLATE gb18030_chinese_ci")
    private String commentText;
    @Column(name = "rating", columnDefinition = "int default 0")
    private int rating = 0;
    @Column(name = "user_name")
    private String userName;
    @PrePersist
    private void initialize() {
        setReviewTime(new Date());
    }

    public ReviewVO toVO() {
        ReviewVO vo = new ReviewVO();
        vo.setItemId(getItem().getItemId());
        vo.setSkuId(getSku().getSkuId());
        vo.setReviewTime(getReviewTime());
        vo.setUserId(getUser().getId());
        vo.setCommentText(getCommentText());
        vo.setRating(getRating());
        vo.setUserName(getUserName());
        return vo;
    }
}
