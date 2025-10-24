package com.seecoder.BlueWhale.repository;

import com.seecoder.BlueWhale.po.Item;
import com.seecoder.BlueWhale.po.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    Page<Review> findByItem(Pageable pageable, Item item);
    Page<Review> findByItemAndRatingGreaterThanEqual(Pageable pageable, Item item, int rating);
    Page<Review> findByItemAndRatingLessThanEqual(Pageable pageable, Item item, int rating);
}
