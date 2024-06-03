package com.fww.utils;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fww.annotation.*;
import com.fww.enumeration.ConditionType;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class QueryBuilder {

    public static <T>QueryWrapper<T> queryBuild(Object baseQuery) throws IllegalAccessException, NoSuchFieldException, InvocationTargetException {
        System.out.println("select1.1");
        boolean result = true;
        Class<?> clazz = baseQuery.getClass();
        Field[] fields = clazz.getDeclaredFields();

        Field needQueryField = null;
        Field inverseField = null;
        Field fuzzyField = null;
        Set<Field> minValueFields = new HashSet<>();
        Set<Field> maxValueFields = new HashSet<>();
        Field excludeField = null;

        for (Field field : fields) {
            if (field.isAnnotationPresent(NeedQuery.class)) {
                if (needQueryField != null) {
                    System.out.println("Error: More than one field with @NeedQuery annotation.");
                    result = false;
                }
                needQueryField = field;
            }
            if (field.isAnnotationPresent(Inverse.class)) {
                if (inverseField != null) {
                    System.out.println("Error: More than one field with @Inverse annotation.");
                    result = false;
                }
                inverseField = field;
            }
            if (field.isAnnotationPresent(Fuzzy.class)) {
                if (fuzzyField != null) {
                    System.out.println("Error: More than one field with @Fuzzy annotation.");
                    result = false;
                }
                fuzzyField = field;
            }
            if (field.isAnnotationPresent(MinValue.class)) {
                minValueFields.add(field);
            }
            if (field.isAnnotationPresent(MaxValue.class)) {
                maxValueFields.add(field);
            }
            if (field.isAnnotationPresent(Exclude.class)) {
                if (excludeField != null) {
                    System.out.println("Error: More than one field with @Exclude annotation.");
                    result = false;
                }
                excludeField = field;
            }
        }
        Object needQueryValue = getValueFromGetter(needQueryField, baseQuery);
        Object inverseValue = getValueFromGetter(inverseField, baseQuery);
        Object fuzzyValue = getValueFromGetter(fuzzyField, baseQuery);
        Map<String, Object> minValues = getMinValues(minValueFields, baseQuery);
        Map<String, Object> maxValues = getMaxValues(maxValueFields, baseQuery);
        Collection<?> excludeValue = getValueFromGetterCollection(excludeField, baseQuery);

        result = result && needQueryValue != null;
        return result ? queryConstructor(needQueryValue, fuzzyValue, inverseValue, minValues, maxValues, excludeValue) :
                new QueryWrapper<>();
    }

    public static <T> LambdaQueryWrapper<T> lambdaQueryBuild(Object baseQuery) throws IllegalAccessException, NoSuchFieldException, InvocationTargetException {
        return (LambdaQueryWrapper<T>) queryBuild(baseQuery).lambda();
    }

    private static <T>QueryWrapper<T> queryConstructor(Object entityObj, Object likeObj, Object neObj, Map<String, Object> minValues, Map<String, Object> maxValues, Collection<?> exclude) throws NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        T entity = (T) entityObj;

        boolean like = likeObj != null && (boolean) likeObj;
        boolean ne = neObj != null && (boolean) neObj;
        exclude = exclude == null ? new ArrayList<>() : exclude;
        QueryFactory<T> QueryFactory = new QueryFactory<>(new QueryWrapper<>());

        Class<?> clazz = entity.getClass();
        TableName tableNameAnnotation = clazz.getAnnotation(TableName.class);
        Set<String> excludedProperties = null;

        if (tableNameAnnotation != null && tableNameAnnotation.excludeProperty().length > 0) {
            excludedProperties = new HashSet<>(Arrays.asList(tableNameAnnotation.excludeProperty()));
        }
        Method[] methods = clazz.getDeclaredMethods();
        String primaryName = null;
        for (Method method : methods) {
            String methodName = method.getName();
            if (methodName.startsWith("get")) {
                Field field;
                String fieldName;
                try {
                    fieldName = toLowerCaseFirst(methodName.substring(3));
                    if(excludedProperties != null && excludedProperties.contains(fieldName)){
                        continue;
                    }
                    field = clazz.getDeclaredField(fieldName);
                } catch (Exception e) {
                    throw new NoSuchFieldException("No such field: " + methodName);
                }
                String paramName = getParamName(field);
                if(field.isAnnotationPresent(TableId.class)){
                    primaryName = getParamName(field);
                }
                Object value = method.invoke(entity);
                if (value != null) {
                    if (field.isAnnotationPresent(LikeQuery.class) && like) {
                        QueryFactory.setStrategy(ConditionType.LIKE).query(paramName, ne, value);
                    } else if (field.isAnnotationPresent(LikeRightQuery.class) && like) {
                        QueryFactory.setStrategy(ConditionType.LIKE_RIGHT).query(paramName, ne, value);
                    } else {
                        QueryFactory.setStrategy(ConditionType.EQ).query(paramName, ne, value);
                    }
                }
                if (field.isAnnotationPresent(BetweenQuery.class)) {
                    QueryFactory.setStrategy(ConditionType.BETWEEN).query(paramName, true, ne, minValues.getOrDefault(fieldName, null), maxValues.getOrDefault(fieldName, null));
                }
                if(field.isAnnotationPresent(Deleted.class)){
                    QueryFactory.setStrategy(ConditionType.EQ).
                            query(paramName, ne || value == null,
                            value == null ? 1 : value);
                }
            }
        }

        var queryWrapper = QueryFactory.getQueryWrapper();

        if (!exclude.isEmpty()) {
            queryWrapper.notIn(primaryName, exclude);
        }

        return queryWrapper;
    }

    private static String getParamName(Field field) {
        String paramName = field.getName();
        if (field.isAnnotationPresent(TableField.class) || field.isAnnotationPresent(TableId.class)) {
            TableField tableFieldAnnotation = field.getAnnotation(TableField.class);
            TableId tableIdAnnotation = field.getAnnotation(TableId.class);
            if (tableFieldAnnotation != null && !tableFieldAnnotation.value().isEmpty()) {
                paramName = tableFieldAnnotation.value();
            }else if(tableIdAnnotation != null && !tableIdAnnotation.value().isEmpty()){
                paramName = tableIdAnnotation.value();
            }
        }
        return paramName;
    }

    private static String toLowerCaseFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    private static Object getValueFromGetter(Field field, Object object) {
        if (field == null) {
            return null;
        }
        String getterName = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        try {
            Method getter = object.getClass().getMethod(getterName);
            return getter.invoke(object);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            System.out.println("Error: Getter method not found for field: " + field.getName());
            return null;
        }
    }

    private static Collection<?> getValueFromGetterCollection(Field field, Object object) {
        if (field == null) {
            return null;
        }
        String getterName = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        try {
            Method getter = object.getClass().getMethod(getterName);
            Object value = getter.invoke(object);
            if (value instanceof Collection) {
                return (Collection<?>) value;
            } else {
                System.out.println("Error: Field with @Exclude annotation is not a Collection.");
                return null;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            System.out.println("Error: Getter method not found for field: " + field.getName());
            return null;
        }
    }

    private static Map<String, Object> getMinValues(Set<Field> fields, Object baseQuery) {
        return fields.stream()
                .filter(field -> getValueFromGetter(field, baseQuery) != null)
                .collect(Collectors.toMap(field -> field.getAnnotation(MinValue.class).value(),
                        field -> getValueFromGetter(field, baseQuery),
                        (value1, value2) -> Math.min(((Number)value1).doubleValue(), ((Number)value2).doubleValue())));
    }

    private static Map<String, Object> getMaxValues(Set<Field> fields, Object baseQuery) {
        return fields.stream()
                .filter(field -> getValueFromGetter(field, baseQuery) != null)
                .collect(Collectors.toMap(field -> field.getAnnotation(MaxValue.class).value(),
                        field -> getValueFromGetter(field, baseQuery),
                        (value1, value2) -> Math.max(((Number)value1).doubleValue(), ((Number)value2).doubleValue())));
    }
}
