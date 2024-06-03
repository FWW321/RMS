package com.fww.strategy.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fww.strategy.QueryStrategy;

public class LikeStrategy implements QueryStrategy {

    @Override
    public <T> void query(QueryWrapper<T> queryWrapper, String column, boolean condition, boolean ne, Object[] values) {
        if (ne) {
            queryWrapper.notLike(condition, column, values[0]);
        } else {
            queryWrapper.like(condition, column, values[0]);
        }
    }
}
