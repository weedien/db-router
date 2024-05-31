package cn.weedien.middleware.db.router.config;

import cn.weedien.middleware.db.router.MethodAroundAdvice;
import cn.weedien.middleware.db.router.RoutingTemplate;
import cn.weedien.middleware.db.router.annotation.DbRouter;
import cn.weedien.middleware.db.router.dynamic.DynamicDataSource;
import cn.weedien.middleware.db.router.dynamic.DynamicMybatisPlugin;
import cn.weedien.middleware.db.router.strategy.IDatabaseRoutingStrategy;
import cn.weedien.middleware.db.router.strategy.impl.DatabaseRoutingHashStrategy;
import cn.weedien.middleware.db.router.toolkit.WordUtil;
import org.aopalliance.aop.Advice;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据源配置解析
 */
@Configuration
@EnableConfigurationProperties(DatabaseRoutingProperties.class)
public class DatabaseRoutingAutoConfig {

    // @Bean(name = "dbRouterJoinPoint")
    // @ConditionalOnMissingBean
    // public DbRouterJoinPoint joinPoint(DbRouterProperties dbRouterProperties, IDBRouterStrategy dbRouterHashStrategy) {
    //     return new DbRouterJoinPoint(dbRouterProperties, dbRouterHashStrategy);
    // }

    @Bean
    @ConditionalOnMissingBean
    public Advisor dbRouterAdvisor(DatabaseRoutingProperties databaseRoutingProperties, IDatabaseRoutingStrategy dbRouterStrategy) {
        AnnotationMatchingPointcut pointcut = new AnnotationMatchingPointcut(DbRouter.class, true);
        Advice advice = new MethodAroundAdvice(databaseRoutingProperties, dbRouterStrategy);
        return new DefaultPointcutAdvisor(pointcut, advice);
    }

    /**
     * Mybatis插件，用于拦截SQL修改表名
     *
     * @return Mybatis拦截器
     */
    @Bean
    public Interceptor plugin() {
        return new DynamicMybatisPlugin();
    }

    /**
     * 动态数据源，支持多个数据源
     *
     * @param databaseRoutingProperties 数据库路由配置
     * @return 数据源
     */
    @Bean
    DataSource dataSource(DatabaseRoutingProperties databaseRoutingProperties) {
        Map<Object, Object> targetDataSources = new HashMap<>();
        // 依次初始化多个数据源 （数据库连接 + 连接池配置）
        databaseRoutingProperties.getDatasource().forEach((key, dbConfig) -> {
            // 将自定义的数据源配置转换为spring的数据源配置，便于初始化数据源
            DataSourceProperties dataSourceProperties = dbConfig.covertToDataSourceProperties();
            DataSource dataSource = dataSourceProperties.initializeDataSourceBuilder().type(dbConfig.getType()).build();
            // 将数据库连接池的配置设置到数据源中
            BeanWrapper beanWrapper = new BeanWrapperImpl(dataSource);
            dbConfig.getPool().forEach((k, v) -> {
                // key为下划线格式，需要转为驼峰格式
                k = WordUtil.middleScoreToCamelCase(k);
                // 通过setter方法设置属性值
                if (beanWrapper.isWritableProperty(k)) {
                    beanWrapper.setPropertyValue(k, v);
                }
            });
            targetDataSources.put(key, dataSourceProperties.initializeDataSourceBuilder().build());
        });
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setTargetDataSources(targetDataSources);
        dynamicDataSource.setDefaultTargetDataSource(targetDataSources.get(databaseRoutingProperties.getConfig().getDefaultDb()));
        return dynamicDataSource;
    }

    /**
     * 数据库路由策略
     * <p>外部常用的接口，传入分片值，切换当前线程的操作的库表信息
     *
     * @param databaseRoutingProperties 数据库路由配置
     * @return 数据库路由策略
     */
    @Bean
    @ConditionalOnMissingBean
    public IDatabaseRoutingStrategy dbRouterHashStrategy(DatabaseRoutingProperties databaseRoutingProperties) {
        return new DatabaseRoutingHashStrategy(databaseRoutingProperties);
    }

    /**
     * 基于动态数据源的事务模板
     *
     * @param dataSource 动态数据源
     * @return 事务模板
     */
    @Bean
    public TransactionTemplate transactionTemplate(DataSource dataSource) {
        return new TransactionTemplate(new DataSourceTransactionManager(dataSource));
    }

    /**
     * 数据库路由查询模板
     *
     * @param databaseRoutingStrategy 数据库路由策略
     * @return 数据库路由查询模板
     */
    @Bean
    public RoutingTemplate routingTemplate(IDatabaseRoutingStrategy databaseRoutingStrategy) {
        return new RoutingTemplate(databaseRoutingStrategy);
    }
}
