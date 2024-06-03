package com.fww.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fww.enumeration.ConditionType;
import com.fww.strategy.QueryStrategy;
import com.fww.strategy.impl.*;
import lombok.Getter;
import lombok.Setter;

public class QueryFactory<T> {
    @Getter
    @Setter
    private QueryWrapper<T> queryWrapper;
    private final static QueryStrategy likeStrategy = new LikeStrategy();

    private final static QueryStrategy likeRightStrategy = new LikeRightStrategy();

    private final static QueryStrategy likeLeftStrategy = new LikeLeftStrategy();

    private final static QueryStrategy eqStrategy = new EqStrategy();

    private final static QueryStrategy leStrategy = new LeStrategy();

    private final static QueryStrategy geStrategy = new GeStrategy();

    private final static QueryStrategy ltStrategy = new LtStrategy();

    private final static QueryStrategy gtStrategy = new GtStrategy();

    private final static QueryStrategy betweenStrategy = new BetweenStrategy();

    private final static QueryStrategy inStrategy = new InStrategy();

    private QueryStrategy strategy;

    public QueryFactory(QueryWrapper<T> queryWrapper) {
        this.queryWrapper = queryWrapper;
    }

    public QueryFactory<T> setStrategy(ConditionType conditionType){
        strategy= switch (conditionType) {
            case LIKE -> likeStrategy;
            case LIKE_LEFT -> likeLeftStrategy;
            case LIKE_RIGHT -> likeRightStrategy;
            case EQ -> eqStrategy;
            case LE -> leStrategy;
            case GE -> geStrategy;
            case LT -> ltStrategy;
            case GT -> gtStrategy;
            case BETWEEN -> betweenStrategy;
            case IN -> inStrategy;
            default -> null;
        };
        return this;
    }

    public ConditionType getStatus(){
        if(strategy == likeStrategy){
            return ConditionType.LIKE;
        } else if(strategy == likeLeftStrategy){
            return ConditionType.LIKE_LEFT;
        } else if(strategy == likeRightStrategy){
            return ConditionType.LIKE_RIGHT;
        } else if (strategy == eqStrategy) {
            return ConditionType.EQ;
        }else if (strategy == leStrategy) {
            return ConditionType.LE;
        }else if (strategy == geStrategy) {
            return ConditionType.GE;
        }else if (strategy == ltStrategy) {
            return ConditionType.LT;
        }else if (strategy == gtStrategy) {
            return ConditionType.GT;
        }else if (strategy == betweenStrategy) {
            return ConditionType.BETWEEN;
        }else if(strategy == inStrategy) {
            return ConditionType.IN;
        }
        else {
            return null;
        }
    }

    public QueryFactory<T> query(String column, boolean condition, boolean ne, Object[] values) {
        strategy.query(queryWrapper, column, condition, ne, values);
        return this;
    }

    public QueryFactory<T> query(String column, boolean ne, Object[] values) {
        boolean condition = true;
        for(Object o : values) {
            condition = condition && (o != null);
        }
        strategy.query(queryWrapper, column, condition, ne, values);
        return this;
    }

    public QueryFactory<T> query(String column, Object[] values) {
        boolean condition = true;
        for(Object o : values) {
            condition = condition && (o != null);
        }
        strategy.query(queryWrapper, column, condition, false, values);
        return this;
    }

    public QueryFactory<T> query(String column, boolean ne, Object value) {
        System.out.println("column = " + column);
        Object[] values = new Object[]{value};
        boolean condition = true;
        for(Object o : values) {
            condition = condition && (o != null);
        }
        strategy.query(queryWrapper, column, condition, ne, values);
        return this;
    }

    public QueryFactory<T> query(String column, boolean ne, Object valueX, Object valueY) {
        Object[] values = new Object[]{valueX, valueY};
        boolean condition = true;
        for(Object o : values) {
            condition = condition && (o != null);
        }
        strategy.query(queryWrapper, column, condition, ne, values);
        return this;
    }

    public QueryFactory<T> query(String column, boolean condition, boolean ne, Object valueX, Object valueY) {
        System.out.println("column = " + column);
        Object[] values = new Object[]{valueX, valueY};
        strategy.query(queryWrapper, column, condition, ne, values);
        return this;
    }

}
