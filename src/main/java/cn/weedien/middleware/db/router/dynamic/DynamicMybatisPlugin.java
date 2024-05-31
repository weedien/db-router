package cn.weedien.middleware.db.router.dynamic;

import cn.weedien.middleware.db.router.DatabaseContextHolder;
import cn.weedien.middleware.db.router.annotation.DbRoutingStrategy;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mybatis拦截器，通过对SQL进行拦截，修改分表信息
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {java.sql.Connection.class, Integer.class})})
public class DynamicMybatisPlugin implements Interceptor {

    // private static final Logger log = LoggerFactory.getLogger(DynamicMybatisPlugin.class);
    private final Pattern pattern = Pattern.compile("(from|into|update)\\s+(\\w+)", Pattern.CASE_INSENSITIVE);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = MetaObject.forObject(statementHandler, SystemMetaObject.DEFAULT_OBJECT_FACTORY, SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");

        // 获取自定义注解判断是否进行分表操作
        // 在xml中配置的id为接口的方法名，在启动时会加上类名，一起作为id
        String id = mappedStatement.getId();
        String className = id.substring(0, id.lastIndexOf("."));
        Class<?> clazz = Class.forName(className);
        DbRoutingStrategy dbRoutingStrategy = clazz.getAnnotation(DbRoutingStrategy.class);
        if (null == dbRoutingStrategy || !dbRoutingStrategy.shardingTable()) {
            return invocation.proceed();
        }

        // 获取SQL
        String sql = statementHandler.getBoundSql().getSql();

        // 替换SQL表名 USER 为 USER_03
        Matcher matcher = pattern.matcher(sql);
        String tableName = null;
        if (matcher.find()) {
            tableName = matcher.group().trim();
        }
        assert tableName != null;
        String replacedSql = matcher.replaceAll(tableName + "_" + DatabaseContextHolder.getTBKey());

        // 通过反射修改SQL
        metaObject.setValue("delegate.boundSql.sql", replacedSql);

        return invocation.proceed();
    }
}
