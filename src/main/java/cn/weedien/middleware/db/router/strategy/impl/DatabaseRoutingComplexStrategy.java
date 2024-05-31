package cn.weedien.middleware.db.router.strategy.impl;

import cn.weedien.middleware.db.router.config.DatabaseRoutingProperties;
import cn.weedien.middleware.db.router.strategy.AbstractDatabaseRoutingStrategy;

/**
 * 数据库路由复合分片策略
 */
public class DatabaseRoutingComplexStrategy extends AbstractDatabaseRoutingStrategy {

    public DatabaseRoutingComplexStrategy(DatabaseRoutingProperties databaseRoutingProperties) {
        super(databaseRoutingProperties);
    }

    @Override
    public int dbIdx(Object dbKeyAttr) {
        return 0;
    }

    @Override
    public int tbIdx(Object dbKeyAttr) {
        return 0;
    }

}
