package com.example.basic.util;

import cn.hutool.core.collection.CollUtil;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 集合工具（封装 Hutool CollUtil）。
 *
 * @author hermes-agent
 */
public class CollUtil {

    public static boolean isEmpty(Collection<?> c) { return CollUtil.isEmpty(c); }
    public static boolean isNotEmpty(Collection<?> c) { return !isEmpty(c); }
    public static boolean isEmpty(Map<?, ?> m) { return m == null || m.isEmpty(); }
    public static boolean isNotEmpty(Map<?, ?> m) { return !isEmpty(m); }

    public static <T> List<T> sub(List<T> list, int from, int size) {
        if (isEmpty(list) || from >= list.size()) return new ArrayList<>();
        return new ArrayList<>(list.subList(from, Math.min(from + size, list.size())));
    }

    @SafeVarargs
    public static <T> List<T> listOf(T... elems) {
        if (elems == null || elems.length == 0) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(elems));
    }

    public static <T, R> List<R> extract(List<T> list, Function<T, R> mapper) {
        if (isEmpty(list)) return new ArrayList<>();
        return list.stream().filter(Objects::nonNull).map(mapper).filter(Objects::nonNull).toList();
    }

    public static <T> T first(List<T> list) { return isNotEmpty(list) ? list.get(0) : null; }
    public static <T> T last(List<T> list)  { return isNotEmpty(list) ? list.get(list.size()-1) : null; }

    public static <T, K> Map<K, List<T>> groupBy(List<T> list, Function<T, K> keyMapper) {
        if (isEmpty(list)) return new LinkedHashMap<>();
        return list.stream().collect(Collectors.groupingBy(keyMapper, LinkedHashMap::new, Collectors.toList()));
    }

    public static <T> List<T> distinct(List<T> list) {
        if (isEmpty(list)) return new ArrayList<>();
        return new ArrayList<>(new LinkedHashSet<>(list));
    }
}
