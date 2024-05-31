package cn.weedien.middleware.db.router.strategy.impl;

import cn.weedien.middleware.db.router.config.DatabaseRoutingProperties;
import cn.weedien.middleware.db.router.strategy.AbstractDatabaseRoutingStrategy;

/**
 * 基于哈希的路由策略
 */
public class DatabaseRoutingHashStrategy extends AbstractDatabaseRoutingStrategy {

    public DatabaseRoutingHashStrategy(DatabaseRoutingProperties databaseRoutingProperties) {
        super(databaseRoutingProperties);
    }

    @Override
    public int dbIdx(Object dbKeyAttr) {
        int size = dbCount() * tbCount();
        int idx = (size - 1) & (dbKeyAttr.hashCode() ^ (dbKeyAttr.hashCode() >>> 16));
        return idx / tbCount();
    }

    @Override
    public int tbIdx(Object dbKeyAttr) {
        int size = dbCount() * tbCount();
        return (size - 1) & (dbKeyAttr.hashCode() ^ (dbKeyAttr.hashCode() >>> 16));
    }
}
