package cn.weedien.middleware.db.router.annotation;

import java.lang.annotation.*;

/**
 * 路由策略，分表标记
 * <p>
 * 默认分库分表，分库不分表这种搭配是不存在的，既然分库了，那肯定至少存在两张表
 * <p>
 * 这个注解要写在Mybatis的Mapper接口上，运行时通过Mybatis的拦截器修改tb
 * 如果提供了配置文件的方式，也需要配置Mapper接口的包路径，因为无法通过数据库的表名称判断运行时需要拦截的Mapper接口
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DbRoutingStrategy {

    /**
     * 是否分表
     */
    boolean shardingTable() default false;

    /**
     * 分表策略
     */
    Class<?> strategy() default Object.class;
}
