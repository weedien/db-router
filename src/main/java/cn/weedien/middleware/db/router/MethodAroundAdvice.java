package cn.weedien.middleware.db.router;

import cn.weedien.middleware.db.router.annotation.DbRouter;
import cn.weedien.middleware.db.router.config.DatabaseRoutingProperties;
import cn.weedien.middleware.db.router.strategy.IDatabaseRoutingStrategy;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MethodAroundAdvice implements MethodBeforeAdvice, AfterReturningAdvice {

    private final Logger log = LoggerFactory.getLogger(MethodAroundAdvice.class);

    private final DatabaseRoutingProperties databaseRoutingProperties;

    private final IDatabaseRoutingStrategy dbRouterStrategy;

    // TODO 支持为不同表配置不同分片策略
    // private final Map<String, IDatabaseRoutingStrategy> routingStrategyMap;

    public MethodAroundAdvice(DatabaseRoutingProperties databaseRoutingProperties, IDatabaseRoutingStrategy dbRouterStrategy) {
        this.databaseRoutingProperties = databaseRoutingProperties;
        this.dbRouterStrategy = dbRouterStrategy;
    }

    @Override
    public void afterReturning(Object returnValue, @Nonnull Method method, @Nonnull Object[] args, Object target) throws Throwable {
        // 清除数据源上下文
        dbRouterStrategy.clear();
    }

    @Override
    public void before(@Nonnull Method method, @Nonnull Object[] args, Object target) throws Throwable {
        // dbKey只有在传入参数中包含分片键的时候才有效，通过分片键可以定位到具体的数据库表
        // 但有些操作涉及到多张表的操作，要判断是否涉及多表操作
        // 1. 插入操作一定要带上分片键值，
        // 2. 更新操作不一定会带分片键，如果传入的参数中分片键的值为空，则需要进行扩散查
        // 3. 删除操作不一定会带分片键，如果传入的参数中分片键的值为空，则需要进行扩散查
        // 4. 查询时可以传入单个或多个参数，也可以传入实体，传入多个参数时需要通过sql才能判断是否存在分片键
        String dbKey = getDbKey(target, method);
        // 查询方法中存在分片键值
        if (dbKey != null) {
            Object dbKeyAttr = getAttrValue(dbKey, args);
            dbRouterStrategy.doRouting(dbKeyAttr);
        }
        // 入参中不存在分片键时，由mybatis查件进行扩散查询
    }

    private String getDbKey(Object target, Method method) {
        if (target != null) {
            DbRouter dbRouter = target.getClass().getInterfaces().getClass().getAnnotation(DbRouter.class);
            if (dbRouter != null) {
                return dbRouter.key();
            }
        }

        DbRouter methodDbRouter = method.getAnnotation(DbRouter.class);
        if (methodDbRouter != null) {
            return methodDbRouter.key();
        }

        return databaseRoutingProperties.getConfig().getRoutingKey();
    }


    private Object getAttrValue(String dbKey, Object[] args) {
        if (args.length == 1 && args[0].getClass().isPrimitive()) {
            return args[0];
        }

        for (Object arg : args) {
            try {
                Object fieldValue = getValueByName(arg, dbKey);
                // TODO 字段为默认值是是否需要处理
                return fieldValue;
            } catch (Exception e) {
                log.error("getAttrValue error", e);
            }
        }
        return null;
    }

    /**
     * 获取对象的特定属性值
     *
     * @param item 对象
     * @param name 属性名
     * @return 属性值
     * @author tang
     */
    private Object getValueByName(Object item, String name) {
        try {
            Field field = getFieldByName(item, name);
            if (field == null) {
                return null;
            }
            field.setAccessible(true);
            Object o = field.get(item);
            field.setAccessible(false);
            return o;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /**
     * 根据名称获取方法，该方法同时兼顾继承类获取父类的属性
     *
     * @param item 对象
     * @param name 属性名
     * @return 该属性对应方法
     * @author tang
     */
    private Field getFieldByName(Object item, String name) {
        try {
            Field field;
            try {
                field = item.getClass().getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                field = item.getClass().getSuperclass().getDeclaredField(name);
            }
            return field;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}
