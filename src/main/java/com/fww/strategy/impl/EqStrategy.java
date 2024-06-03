package com.fww.strategy.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fww.strategy.QueryStrategy;

public class EqStrategy implements QueryStrategy {
     @Override
     public <T> void query(QueryWrapper<T> queryWrapper, String column, boolean condition, boolean ne, Object[] values) {
         if(ne){
             queryWrapper.ne(condition, column, values[0]);
         }else {
             queryWrapper.eq(condition, column, values[0]);
         }
     }
}
