package com.fww.strategy.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fww.strategy.QueryStrategy;

public class BetweenStrategy implements QueryStrategy {
    @Override
    public <T> void query(QueryWrapper<T> queryWrapper, String column, boolean condition, boolean ne, Object[] values) {

        int nullNum = 0;

        for(Object o : values){
            if(o == null){
                nullNum++;
            }
        }

        if(nullNum == values.length || values.length > 2){
            return;
        }

        boolean flag = nullNum == 0 && values.length == 2;

        if (ne) {
            if(flag){
                queryWrapper.notBetween(condition, column, values[0], values[1]);
            }else if(values[0] != null){
                queryWrapper.lt(condition, column, values[0]);
            }else {
                queryWrapper.gt(condition, column, values[1]);
            }
        } else {
            if(flag){
                queryWrapper.between(condition, column, values[0], values[1]);
            }else if(values[0] != null){
                queryWrapper.ge(condition, column, values[0]);
            }else {
                queryWrapper.le(condition, column, values[1]);
            }
        }
    }
}
