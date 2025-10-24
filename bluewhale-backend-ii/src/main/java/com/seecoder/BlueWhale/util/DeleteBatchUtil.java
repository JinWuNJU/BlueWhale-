package com.seecoder.BlueWhale.util;

import com.seecoder.BlueWhale.exception.BlueWhaleException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.function.Function;

public class DeleteBatchUtil<T> {
    public void deleteBatch(String[] ids, JpaRepository<T, Integer> repo, Function<Integer, T> findByIdMethod) {
        if (ids == null || ids.length == 0) {
            throw BlueWhaleException.illegalParameter();
        }
        for (String item : ids) {
            if (!NumberUtil.isInteger(item)) {
                throw BlueWhaleException.illegalParameter();
            }
            T entity = findByIdMethod.apply(Integer.parseInt(item));
            if (entity == null) {
                throw BlueWhaleException.illegalParameter();
            }
            repo.delete(entity);
        }
    }
}
