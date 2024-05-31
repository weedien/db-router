package cn.weedien.middleware.db.router.dynamic;

import cn.weedien.middleware.db.router.DatabaseContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicDataSource extends AbstractRoutingDataSource {

    public static final String DATA_SOURCE_PREFIX = "ds_";

    @Value("${db-router.config.default-db:ds_0}")
    private String defaultDataSource;

    /**
     * 这个方法在每次从数据源中获取Connection时都会被调用，
     * 所以只需要修改DatabaseContextHolder中的dbKey即可实现动态切换数据源
     *
     * @return 数据源的key
     */
    @Override
    protected Object determineCurrentLookupKey() {
        if (DatabaseContextHolder.getDBKey() == null || DatabaseContextHolder.getDBKey().isEmpty()) {
            return defaultDataSource;
        }
        return DATA_SOURCE_PREFIX + DatabaseContextHolder.getDBKey();
    }
}
