package com.sky.axon.common.config.axon;

import com.mongodb.client.MongoCollection;
import com.sky.axon.common.config.mongo.DataSourceContext;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

/**
 * @author demo
 */
@Slf4j
public class CustomAxonMongoTemplate implements org.axonframework.extensions.mongo.MongoTemplate {

    @Setter
    private String domainEventsCollectionName = "domainevents";
    @Setter
    private String snapshotEventsCollectionName = "snapshotevents";
    @Setter
    private String trackingTokensCollectionName = "trackingtokens";
    @Setter
    private String sagasCollectionName = "sagas";

    //todo 多数据源动态路由容器
    private List<MongoTemplate> list;

    public CustomAxonMongoTemplate(List<MongoTemplate> list) {
        this.list = list;
    }

    @Override
    public MongoCollection<Document> trackingTokensCollection() {
        return list.get(0).getCollection(trackingTokensCollectionName);
    }

    @Override
    public MongoCollection<Document> eventCollection() {
        //TODO 启动时创建索引会调用
        String dataSource = DataSourceContext.getDataSource();
        log.info("eventCollection datasource:{}", dataSource);
        return list.get(0).getCollection(domainEventsCollectionName);
    }

    @Override
    public MongoCollection<Document> snapshotCollection() {
        String dataSource = DataSourceContext.getDataSource();
        log.info("snapshotCollection datasource:{}" + dataSource);
        return list.get(0).getCollection(snapshotEventsCollectionName);
    }

    @Override
    public MongoCollection<Document> sagaCollection() {
        return list.get(0).getCollection(sagasCollectionName);
    }
}
