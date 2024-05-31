package cn.weedien.middleware.db.router;

import cn.weedien.middleware.db.router.annotation.DbRouter;
import cn.weedien.middleware.db.router.annotation.DbRoutingStrategy;

@DbRoutingStrategy(shardingTable = true)
public interface IUserDao {

    @DbRouter(key = "userId")
    void insert(String req);
}
