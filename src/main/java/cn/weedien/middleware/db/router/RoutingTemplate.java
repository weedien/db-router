package cn.weedien.middleware.db.router;

import cn.weedien.middleware.db.router.annotation.DbRouter;
import cn.weedien.middleware.db.router.strategy.IDatabaseRoutingStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 只适用于配置了分库或分表的操作，
 * 单表操作会被路由到默认数据源
 */
public class RoutingTemplate {

    private final IDatabaseRoutingStrategy databaseRoutingStrategy;

    public RoutingTemplate(IDatabaseRoutingStrategy databaseRoutingStrategy) {
        this.databaseRoutingStrategy = databaseRoutingStrategy;
    }

    /**
     * 分库分表的扩散查询
     * <p>
     * 只分库不分表这种情况可以理解为：分表数为1
     *
     * @param queryTask 查询任务
     * @param <T>       查询结果类型
     * @return 聚合查询结果
     */
    public <T> List<T> scatterQuery(Supplier<List<T>> queryTask) {
        int dbCount = databaseRoutingStrategy.dbCount();
        int tableCount = databaseRoutingStrategy.tbCount();

        List<T> aggregateResult = new ArrayList<>();
        for (int i = 0; i < dbCount; i++) {
            for (int j = 0; j < tableCount; j++) {
                databaseRoutingStrategy.setDBKey(i);
                databaseRoutingStrategy.setTBKey(j);
                List<T> result = queryTask.get();
                if (result != null && !result.isEmpty()) {
                    aggregateResult.addAll(result);
                }
            }
        }
        databaseRoutingStrategy.clear();

        return aggregateResult;
    }

    /**
     * 不分库只分表的扩散查询
     *
     * @param dbIdx     数据库索引, 如ds_0
     * @param queryTask 查询任务
     * @param <T>       查询结果类型
     * @return 聚合查询结果
     */
    public <T> List<T> scatterQueryWithGivenDb(int dbIdx, Supplier<List<T>> queryTask) {
        int tableCount = databaseRoutingStrategy.tbCount();
        databaseRoutingStrategy.setDBKey(dbIdx); // dbIdx固定不变

        List<T> aggregateResult = new ArrayList<>();
        for (int i = 0; i < tableCount; i++) {
            databaseRoutingStrategy.setTBKey(i);
            List<T> result = queryTask.get();
            if (result != null && !result.isEmpty()) {
                aggregateResult.addAll(result);
            }
        }
        databaseRoutingStrategy.clear();

        return aggregateResult;
    }

    /**
     * 分片路由查询
     * <p>
     * 主要用于插入、更新、删除等操作
     * <p>
     * 使用{@link DbRouter}注解标记的方法或类，
     * 会通过AOP的方式自动路由，达到与本方法等效的结果
     *
     * @param shardingKey 分片键
     * @param task        查询任务
     * @param <T>         查询结果类型
     * @return 查询结果
     */
    public <T> T execute(String shardingKey, Supplier<T> task) {
        databaseRoutingStrategy.doRouting(shardingKey);
        T result = task.get();
        databaseRoutingStrategy.clear();
        return result;
    }

}
