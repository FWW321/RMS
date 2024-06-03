package com.fww.strategy;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

public interface QueryStrategy {
    <T> void query(QueryWrapper<T> lambdaQueryWrapper, String column, boolean condition, boolean ne, Object[] values);
}
