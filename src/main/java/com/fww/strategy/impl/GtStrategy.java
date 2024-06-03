package com.fww.strategy.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fww.strategy.QueryStrategy;

public class GtStrategy implements QueryStrategy {
    @Override
    public <T> void query(QueryWrapper<T> lambdaQueryWrapper, String column, boolean condition, boolean ne, Object[] values) {
        if (ne) {
            lambdaQueryWrapper.le(condition, column, values[0]);
        } else {
            lambdaQueryWrapper.gt(condition, column, values[0]);
        }
    }
}
