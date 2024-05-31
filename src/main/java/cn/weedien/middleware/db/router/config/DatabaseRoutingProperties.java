package cn.weedien.middleware.db.router.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据路由配置
 */
@ConfigurationProperties(prefix = "db-router", ignoreInvalidFields = true)
public class DatabaseRoutingProperties {

    public static final String PREFIX = "db-router";

    private Config config = new Config();
    private Map<String, CustomDataSourceProperties> datasource = new HashMap<>();

    public void setConfig(Config config) {
        this.config = config;
    }

    public void setDatasource(Map<String, CustomDataSourceProperties> datasource) {
        this.datasource = datasource;
    }

    public Config getConfig() {
        return config;
    }

    public Map<String, CustomDataSourceProperties> getDatasource() {
        return datasource;
    }

    public static class Config {
        private Integer dbCount;
        private Integer tbCount;
        private String defaultDb;
        private String routingKey;
        private String list;

        public String getDefaultDb() {
            return defaultDb;
        }

        public Integer getDbCount() {
            return dbCount;
        }

        public Integer getTbCount() {
            return tbCount;
        }

        public String getRoutingKey() {
            return routingKey;
        }

        public String getList() {
            return list;
        }

        public void setTbCount(Integer tbCount) {
            this.tbCount = tbCount;
        }

        public void setDbCount(Integer dbCount) {
            this.dbCount = dbCount;
        }

        public void setRoutingKey(String routingKey) {
            this.routingKey = routingKey;
        }

        public void setDefaultDb(String defaultDb) {
            this.defaultDb = defaultDb;
        }

        public void setList(String list) {
            this.list = list;
        }
    }

    public static class CustomDataSourceProperties {
        private String driverClassName;
        private String url;
        private String username;
        private String password;
        private String typeClassName;
        private Class<? extends DataSource> type;
        @NestedConfigurationProperty
        private Map<String, Object> pool = new HashMap<>();

        public CustomDataSourceProperties() {
        }

        public DataSourceProperties covertToDataSourceProperties() {
            DataSourceProperties dataSourceProperties = new DataSourceProperties();
            dataSourceProperties.setUrl(this.url);
            dataSourceProperties.setUsername(this.username);
            dataSourceProperties.setPassword(this.password);
            dataSourceProperties.setDriverClassName(this.driverClassName);
            return dataSourceProperties;
        }

        public void setPool(Map<String, Object> pool) {
            this.pool = pool;
        }

        public Map<String, Object> getPool() {
            return pool;
        }

        public Class<? extends DataSource> getType() {
            return type;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        public String getUrl() {
            return url;
        }

        public String getPassword() {
            return password;
        }

        public String getUsername() {
            return username;
        }

        public String getTypeClassName() {
            return typeClassName;
        }

        public void setType(Class<? extends DataSource> type) {
            this.type = type;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setTypeClassName(String typeClassName) {
            this.typeClassName = typeClassName;
        }
    }
}
