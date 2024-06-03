package com.fww.strategy.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fww.strategy.QueryStrategy;

public class InStrategy implements QueryStrategy {

    @Override
    public <T> void query(QueryWrapper<T> queryWrapper, String column, boolean condition, boolean ne, Object[] values) {
        if (ne) {
            queryWrapper.notIn(condition, column, values);
        } else {
            queryWrapper.in(condition, column, values);
        }
    }
}
