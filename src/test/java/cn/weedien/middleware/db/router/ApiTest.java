package cn.weedien.middleware.db.router;

import cn.weedien.middleware.db.router.config.DatabaseRoutingProperties;
import cn.weedien.middleware.db.router.strategy.impl.DatabaseRoutingHashStrategy;
import org.junit.jupiter.api.Test;

class ApiTest {

    @Test
    public void test_db_hash() {
        String key = "weedien";

        int dbCount = 2, tbCount = 4;
        int size = dbCount * tbCount;
        // 散列
        int idx = (size - 1) & (key.hashCode() ^ (key.hashCode() >>> 16));

        // int dbIdx = idx / tbCount + 1;
        // int tbIdx = idx - tbCount * (dbIdx - 1);

        int dbIdx = idx / tbCount;
        int tbIdx = idx;

        System.out.println(dbIdx);
        System.out.println(tbIdx);

    }

    @Test
    public void test_RoutingTemplate() {
        DatabaseRoutingProperties properties = new DatabaseRoutingProperties();
        properties.setConfig(new DatabaseRoutingProperties.Config() {{
            setDbCount(2);
            setTbCount(4);
        }});
        RoutingTemplate routingTemplate = new RoutingTemplate(new DatabaseRoutingHashStrategy(properties));
        routingTemplate.execute("weedien", () -> {
            System.out.println("Execute task");
            return null;
        });
    }

}
