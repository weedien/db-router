package cn.weedien.middleware.db.router.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 路由注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DbRouter {
    @AliasFor("key")
    String value() default "";

    /**
     * 分库分表字段
     */
    String key() default "";
}
