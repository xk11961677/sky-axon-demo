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
package com.sky.axon.common.annotation;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 查询媒介
 * 1. equals：相等
 * 2. like:mongodb的like查询
 * 3. in:用于列表的in类型查询
 *
 * @author
 */
public enum QueryType {
    EQUALS {
        @Override
        public Criteria buildCriteria(QueryField queryFieldAnnotation, Field field, Object value) {
            if (check(queryFieldAnnotation, field, value)) {
                String queryField = getQueryFieldName(queryFieldAnnotation, field);
                return Criteria.where(queryField).is(value.toString());
            }
            return new Criteria();
        }
    },
    LIKE {
        @Override
        public Criteria buildCriteria(QueryField queryFieldAnnotation, Field field, Object value) {
            if (check(queryFieldAnnotation, field, value)) {
                String queryField = getQueryFieldName(queryFieldAnnotation, field);
                return Criteria.where(queryField).regex(value.toString());
            }
            return new Criteria();
        }
    },
    IN {
        @Override
        public Criteria buildCriteria(QueryField queryFieldAnnotation, Field field, Object value) {
            if (check(queryFieldAnnotation, field, value)) {
                if (value instanceof List) {
                    String queryField = getQueryFieldName(queryFieldAnnotation, field);
                    // 此处必须转型为List，否则会在in外面多一层[]
                    return Criteria.where(queryField).in((List<?>) value);
                }
            }
            return new Criteria();
        }
    };

    private static boolean check(QueryField queryField, Field field, Object value) {
        return !(queryField == null || field == null || value == null);
    }

    public abstract Criteria buildCriteria(QueryField queryFieldAnnotation, Field field, Object value);


    /**
     * 如果实体bean的字段上QueryField注解没有设置attribute属性时，默认为该字段的名称
     *
     * @param field
     * @return
     */
    private static String getQueryFieldName(QueryField queryField, Field field) {
        String queryFieldValue = queryField.attribute();
        if (!StringUtils.hasText(queryFieldValue)) {
            queryFieldValue = field.getName();
        }
        return queryFieldValue;
    }
}

