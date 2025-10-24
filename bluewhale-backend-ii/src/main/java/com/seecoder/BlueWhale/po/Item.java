package com.seecoder.BlueWhale.po;

import com.seecoder.BlueWhale.enums.ItemCategory;
import com.seecoder.BlueWhale.vo.ItemBasicVO;
import com.seecoder.BlueWhale.vo.ItemVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(indexes = {
        @Index(columnList = "create_time"),
        @Index(columnList = "item_price"),
        @Index(columnList = "item_rating")
})
// @Table (uniqueConstraints = { @UniqueConstraint(columnNames = { "item_name", "store_id" }) }) 允许重复创建物品比较方便测试
public class Item {
    /* 实体成员 */
    @ManyToOne(fetch = FetchType.LAZY)   // manyToOne的默认行为是eager fetch，设成lazy减少查询次数（在仅仅获取外键值时不会产生额外查询）
    @JoinColumn(name = "store_id", referencedColumnName = "store_id")
    private Store store;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private int itemId;
    @Column(name = "item_name", columnDefinition = "varchar(255) CHARACTER SET gb18030 COLLATE gb18030_chinese_ci, FULLTEXT KEY item_name_full_text_index (item_name) WITH PARSER ngram")
    private String itemName;                                                                                       /* 全文索引，用ngram分词配合全文索引 */
    @Column(name = "item_desc")
    private String itemDesc;
    @Column(name = "item_price")
    private String itemPrice;
    @Formula(value = "CAST(item_price AS DECIMAL(10, 2))")
    private BigDecimal itemPriceDecimal;
    @Column(name = "item_category")
    private ItemCategory itemCategory;
    @ManyToMany(fetch = FetchType.EAGER)       // 商品的基础信息必定包含封面图，故使用eager fetch（默认为lazy）
    @JoinTable(
            name = "item_image_list",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "url")
    )
    private Set<ImageInfo> itemImage;
    @Column(name = "create_time")
    private Date createTime;
    @Column(name = "item_sold_info")
    private String itemSoldInfo;
    @ManyToMany
    @JoinTable(
            name = "item_image_desc_list",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "url")
    )
    private Set<ImageInfo> itemDescImage;
    @OneToMany(mappedBy = "item", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private Set<Sku> sku;
    @Column(name = "item_sold")
    private String itemSold;
    @Column(name = "item_rating", columnDefinition = "double default 0")
    private double itemRating = 0;
    @Column(name = "item_review_count", columnDefinition = "int default 0")
    private int itemReviewCount = 0;
    /* 实体持久化相关 */
    /* 设置创建时间 */
    @PrePersist
    private void initialize() {
        setCreateTime(new Date());
    }
    /* 释放图片引用 */
    @PreRemove
    public void freeAll() {
        freeImgs(itemImage);
        freeImgs(itemDescImage);
    }
    private void freeImgs(Set<ImageInfo> imgs) {
        if (imgs == null) return;
        imgs.forEach(ImageInfo::free);
    }
    /* 新图片被设置后更新图片引用状态 */
    public void setItemImage(Set<ImageInfo> itemImage) {
        if (this.itemImage != null)
            this.itemImage.forEach(ImageInfo::free);
        this.itemImage = itemImage;
        this.itemImage.forEach(ImageInfo::use);
    }
    public void setItemDescImage(Set<ImageInfo> ItemDescImage) {
        if (this.itemDescImage != null)
            this.itemDescImage.forEach(ImageInfo::free);
        this.itemDescImage = ItemDescImage;
        this.itemDescImage.forEach(ImageInfo::use);
    }
    /* 计算所有sku中的最低价格 */
    public void setSku(Set<Sku> sku) {
        /**
         * @see https://stackoverflow.com/questions/5587482/hibernate-a-collection-with-cascade-all-delete-orphan-was-no-longer-referenc
         */
        if (this.sku == null)
            this.sku = sku;
        else {
            this.sku.retainAll(sku);
            this.sku.addAll(sku);
        }
        double lowest = Double.MAX_VALUE;
        for (Sku s : this.sku) {
            lowest = Double.min(Double.parseDouble(s.getSkuPrice()), lowest);
        }
        setItemPrice(String.valueOf(lowest));
    }



    public ItemBasicVO toItemBasicVO() {
        ItemBasicVO itemBasicVO = new ItemBasicVO();
        itemBasicVO.setStoreId(getStore().getStoreId());
        itemBasicVO.setItemRating(getItemRating());
        itemBasicVO.setItemReviewCount(getItemReviewCount());
        itemBasicVO.setItemId(getItemId());
        itemBasicVO.setItemName(getItemName());
        itemBasicVO.setItemDesc(getItemDesc());
        itemBasicVO.setItemPrice(getItemPrice());
        itemBasicVO.setItemCategory(getItemCategory());
        itemBasicVO.setItemRating(Double.parseDouble(String.format("%.2f", getItemRating())));
        itemBasicVO.setItemReviewCount(getItemReviewCount());
        itemBasicVO.setItemImage(getItemImage().stream().map(ImageInfo::getUrl).collect(Collectors.toSet()));
        return itemBasicVO;
    }

    public ItemVO toItemVO() {
        ItemVO itemVO = new ItemVO();
        itemVO.fromBasic(toItemBasicVO());
        itemVO.setItemCreateTime(getCreateTime());
        itemVO.setItemSoldInfo(getItemSoldInfo());
        if (getItemDescImage() != null)
            itemVO.setItemDescImage(getItemDescImage().stream().map(ImageInfo::getUrl).collect(Collectors.toSet()));
        itemVO.setItemSku(getSku().stream().map(Sku::toVO).collect(Collectors.toSet()));
        return itemVO;
    }

}