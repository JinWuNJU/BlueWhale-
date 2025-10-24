package com.seecoder.BlueWhale.vo;

import com.seecoder.BlueWhale.po.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class ReviewVO {
    private int itemId;
    private int skuId;
    private Date reviewTime;
    private int userId;
    @Size(min = 1, max = 200)
    private String commentText;
    @Min(1)
    @Max(5)
    private int rating;
    private String userName;

    public Review toPO(OrderInfo orderInfo, User user, Item item, Sku sku) {
        Review review = new Review();
        review.setCommentText(getCommentText());
        review.setRating(getRating());
        review.setOrderInfo(orderInfo);
        review.setUser(user);
        review.setUserName(user.getName());
        review.setItem(item);
        review.setSku(sku);
        return review;
    }
}
