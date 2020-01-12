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
package com.sky.axon.query.base;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

/**
 * 基本CRUD操作接口
 *
 * @author
 */
public interface BaseRepository<T> {

    /**
     * 保存一个对象到mongodb
     *
     * @param entity
     * @return
     */
    T save(T entity);

    /**
     * 根据id删除对象
     *
     * @param t
     */
    void deleteById(T t);

    /**
     * 根据对象的属性删除
     *
     * @param t
     */
    void deleteByCondition(T t);


    /**
     * 根据id进行更新
     *
     * @param id
     * @param t
     */
    void updateById(String id, T t);


    /**
     * 根据对象的属性查询
     *
     * @param t
     * @return
     */
    List<T> findByCondition(T t);


    /**
     * 通过条件查询实体(集合)
     *
     * @param query
     */
    List<T> find(Query query);

    /**
     * 通过一定的条件查询一个实体
     *
     * @param query
     * @return
     */
    T findOne(Query query);

    /**
     * 通过条件查询更新数据
     *
     * @param query
     * @param update
     * @return
     */
    void update(Query query, Update update);

    /**
     * 通过ID获取记录
     *
     * @param id
     * @return
     */
    T findById(String id);

    /**
     * 通过ID获取记录,并且指定了集合名(表的意思)
     *
     * @param id
     * @param collectionName 集合名
     * @return
     */
    T findById(String id, String collectionName);

    /**
     * 通过条件查询,查询分页结果
     *
     * @param page
     * @param query
     * @return
     */
    Page<T> findPage(Page<T> page, Query query);

    /**
     * 求数据总和
     *
     * @param query
     * @return
     */
    long count(Query query);


    /**
     * 获取MongoDB模板操作
     *
     * @return
     */
    MongoTemplate getMongoTemplate();
}
