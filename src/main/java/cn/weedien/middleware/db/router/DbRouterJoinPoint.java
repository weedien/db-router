// package cn.weedien.middleware.db.router;
//
// import cn.weedien.middleware.db.router.annotation.DbRouter;
// import cn.weedien.middleware.db.router.config.DbRouterProperties;
// import cn.weedien.middleware.db.router.strategy.IDBRouterStrategy;
// import org.aspectj.lang.ProceedingJoinPoint;
// import org.aspectj.lang.annotation.Around;
// import org.aspectj.lang.annotation.Aspect;
// import org.aspectj.lang.annotation.Pointcut;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.util.ObjectUtils;
// import org.springframework.util.StringUtils;
//
// import java.lang.reflect.Field;
//
// @Aspect
// public class DbRouterJoinPoint {
//     private final Logger log = LoggerFactory.getLogger(DbRouterJoinPoint.class);
//
//     private final DbRouterProperties dbRouterProperties;
//
//     private final IDBRouterStrategy dbRouterHashStrategy;
//
//     public DbRouterJoinPoint(DbRouterProperties dbRouterProperties, IDBRouterStrategy dbRouterHashStrategy) {
//         this.dbRouterProperties = dbRouterProperties;
//         this.dbRouterHashStrategy = dbRouterHashStrategy;
//     }
//
//     @Pointcut("@annotation(cn.weedien.middleware.db.router.annotation.DbRouter)")
//     public void joinPoint() {
//     }
//
//     @Around("joinPoint() && @annotation(dbRouter)")
//     public Object doRouting(ProceedingJoinPoint jp, DbRouter dbRouter) {
//         String dbKey = dbRouter.key();
//         if (ObjectUtils.isEmpty(dbKey) && ObjectUtils.isEmpty(dbRouterProperties.getConfig().getRoutingKey())) {
//             throw new RuntimeException("annotation DbRouter key is null！");
//         }
//         dbKey = StringUtils.hasLength(dbKey) ? dbKey : dbRouterProperties.getConfig().getRoutingKey();
//         Object dbKeyAttr = getAttrValue(dbKey, jp.getArgs());
//         dbRouterHashStrategy.doRouting(dbKeyAttr);
//         try {
//             return jp.proceed();
//         } catch (Throwable throwable) {
//             log.error("doRouting error", throwable);
//             throw new RuntimeException("doRouting error");
//         } finally {
//             dbRouterHashStrategy.clear();
//         }
//     }
//
//     private Object getAttrValue(String dbKey, Object[] args) {
//         if (args.length == 1) {
//             return args[0];
//         }
//
//         for (Object arg : args) {
//             try {
//                 Object fieldValue = getValueByName(arg, dbKey);
//                 // TODO 字段为默认值是是否需要处理
//                 return fieldValue;
//             } catch (Exception e) {
//                 log.error("getAttrValue error", e);
//             }
//         }
//         return null;
//     }
//
//     /**
//      * 获取对象的特定属性值
//      *
//      * @param item 对象
//      * @param name 属性名
//      * @return 属性值
//      * @author tang
//      */
//     private Object getValueByName(Object item, String name) {
//         try {
//             Field field = getFieldByName(item, name);
//             if (field == null) {
//                 return null;
//             }
//             field.setAccessible(true);
//             Object o = field.get(item);
//             field.setAccessible(false);
//             return o;
//         } catch (IllegalAccessException e) {
//             return null;
//         }
//     }
//
//     /**
//      * 根据名称获取方法，该方法同时兼顾继承类获取父类的属性
//      *
//      * @param item 对象
//      * @param name 属性名
//      * @return 该属性对应方法
//      * @author tang
//      */
//     private Field getFieldByName(Object item, String name) {
//         try {
//             Field field;
//             try {
//                 field = item.getClass().getDeclaredField(name);
//             } catch (NoSuchFieldException e) {
//                 field = item.getClass().getSuperclass().getDeclaredField(name);
//             }
//             return field;
//         } catch (NoSuchFieldException e) {
//             return null;
//         }
//     }
// }
