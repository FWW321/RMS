package com.fww.strategy.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fww.strategy.QueryStrategy;

public class LikeRightStrategy implements QueryStrategy {
    @Override
    public <T> void query(QueryWrapper<T> queryWrapper, String column, boolean condition, boolean ne, Object[] values) {
        if (ne) {
            queryWrapper.notLikeRight(condition, column, values[0]);
        } else {
            queryWrapper.likeRight(condition, column, values[0]);
        }
    }
}
