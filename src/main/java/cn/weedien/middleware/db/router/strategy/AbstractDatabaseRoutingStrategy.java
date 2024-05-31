package cn.weedien.middleware.db.router.strategy;

import cn.weedien.middleware.db.router.DatabaseContextHolder;
import cn.weedien.middleware.db.router.config.DatabaseRoutingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDatabaseRoutingStrategy implements IDatabaseRoutingStrategy {

    Logger log = LoggerFactory.getLogger(AbstractDatabaseRoutingStrategy.class);

    private final DatabaseRoutingProperties databaseRoutingProperties;

    protected AbstractDatabaseRoutingStrategy(DatabaseRoutingProperties databaseRoutingProperties) {
        this.databaseRoutingProperties = databaseRoutingProperties;
    }

    public abstract int dbIdx(Object dbKeyAttr);

    public abstract int tbIdx(Object dbKeyAttr);

    @Override
    public void doRouting(Object dbKeyAttr) {
        if (dbKeyAttr == null) {
            throw new RuntimeException("路由字段不能为空");
        }

        int dbIdx = dbIdx(dbKeyAttr);
        int tbIdx = tbIdx(dbKeyAttr);

        DatabaseContextHolder.setDBKey(String.valueOf(dbIdx));
        DatabaseContextHolder.setTBKey(String.valueOf(tbIdx));
        log.debug("数据库路由: { dbIdx: {}, tbIdx: {} }", dbIdx, tbIdx);
    }

    @Override
    public void setDBKey(int dbIdx) {
        DatabaseContextHolder.setDBKey(String.valueOf(dbIdx));
    }

    @Override
    public void setTBKey(int tbIdx) {
        DatabaseContextHolder.setTBKey(String.valueOf(tbIdx));
    }

    @Override
    public int dbCount() {
        return databaseRoutingProperties.getConfig().getDbCount();
    }

    @Override
    public int tbCount() {
        return databaseRoutingProperties.getConfig().getTbCount();
    }

    @Override
    public void clear() {
        DatabaseContextHolder.clearDBKey();
        DatabaseContextHolder.clearTBKey();
    }
}
