/*
 * The MIT License (MIT)
 * Copyright © 2020 <sky>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sky.axon.common.util;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * MongoDB操作工具类
 *
 * @author luchao
 * @date 2020/1/8
 */
public class MongoOperationUtils {

    /**
     * 根据对象获得mongodb Update语句
     * 除id字段以外，所有被赋值的字段都会成为修改项
     */
    public static Update getUpdateObj(final Object obj) {
        if (obj == null)
            return null;
        Field[] fields = obj.getClass().getDeclaredFields();
        Update update = null;
        boolean isFirst = true;
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (value != null) {
                    if ("id".equals(field.getName().toLowerCase()) || "serialversionuid".equals(field.getName().toLowerCase())) {
                        continue;
                    }
                    if (isFirst) {
                        update = Update.update(field.getName(), value);
                        isFirst = false;
                    } else {
                        update = update.set(field.getName(), value);
                    }
                }

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return update;
    }

    /**
     * 根据对象获得mongodb Query语句
     * <p>
     * 1.时间范围查询：在时间字段前增加begin或end，为这两个字段分别赋值
     * 例：private Date createDate;
     * 开始时间
     * private Date beginCreateDate;
     * 结束时间
     * private Date endCreateDate;
     * 分析后结果：where createDate >= beginCreateDate and createDate < beginCreateDate
     * <p>
     * 2.排序
     * 定义并赋值VO中 orderBy 字段，以英文“,”分割多个排序，以空格分隔排序方向 asc可不写
     * 例：private String orderBy;
     * orderBy="createDate desc,sendDate asc,id"
     * 分析结构：order by createDate desc,sendDate asc,id asc
     * <p>
     * 3.固定值搜索
     * 定义并赋值VO中的任意字段，搜索时会把以赋值的字段当作为搜索条件
     */
    public static Query getQueryObj(final Object obj) {
        if (obj == null)
            return null;
        Field[] fields = obj.getClass().getDeclaredFields();
        // Sort sort=new Sort(new Order(Direction.DESC,"createDate"));
        Query query = new Query();
        //存放日期范围或者确定日期
        Map<String, Criteria> dateMap = new HashMap<String, Criteria>();
        String sortStr = null;
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (value != null) {
                    if ("serialversionuid".equals(field.getName().toLowerCase())) {
                        continue;
                    }
                    if ("orderby".equals(field.getName().toLowerCase())) {
                        sortStr = String.valueOf(value);
                        continue;
                    }
                    //如果是日期类型
                    if (field.getType().getSimpleName().equals("Date")) {
                        if (field.getName().toLowerCase().startsWith("begin")) {
                            String beginName = field.getName().substring(5);
                            if (beginName.isEmpty()) {
                                dateMap.put("begin", Criteria.where("begin").is(value));
                            } else {
                                beginName = StringUtil.toLowerCaseFirstOne(beginName);
                                Criteria criteria = dateMap.get(beginName) == null ? Criteria.where(beginName).gte(value) : dateMap.get(beginName).gte(value);
                                dateMap.put(beginName, criteria);
                            }
                            continue;
                        }
                        if (field.getName().toLowerCase().startsWith("end")) {
                            String endName = field.getName().substring(3);
                            if (endName.isEmpty()) {
                                dateMap.put("end", Criteria.where("end").is(value));
                            } else {
                                endName = StringUtil.toLowerCaseFirstOne(endName);
                                Criteria criteria = dateMap.get(endName) == null ? Criteria.where(endName).lt(value) : dateMap.get(endName).lt(value);
                                dateMap.put(endName, criteria);
                            }
                            continue;
                        }
                        dateMap.put(field.getName(), Criteria.where(field.getName()).is(value));
                        continue;
                    }
                    query.addCriteria(Criteria.where(field.getName()).is(value));
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }        //日期类型查询条件
        for (String key : dateMap.keySet()) {
            if (dateMap.get(key) != null) {
                query.addCriteria(dateMap.get(key));
            }
        }
        //排序
        if (sortStr != null && !sortStr.trim().isEmpty()) {
            Sort sort = null;
            String[] strs = sortStr.split(",");
            for (String str : strs) {
                str = str.trim();
                if (str.isEmpty()) {
                    continue;
                }
                int i = str.indexOf(" ");
                if (i < 0) {
                    if (sort == null) {
                        sort = new Sort(Sort.Direction.ASC, str);
                    } else {
                        sort = sort.and(new Sort(Sort.Direction.ASC, str));
                    }
                } else {
                    String name = str.substring(0, i);
                    String dire = str.substring(i + 1).trim();
                    Sort sn = null;
                    if ("desc".equals(dire.toLowerCase())) {
                        sn = new Sort(Sort.Direction.DESC, name);
                    } else {
                        sn = new Sort(Sort.Direction.ASC, name);
                    }
                    if (sort == null) {
                        sort = sn;
                    } else {
                        sort = sort.and(sn);
                    }
                }
            }
            if (sort != null) {
                query.with(sort);
            }
        }
        return query;
    }
}
