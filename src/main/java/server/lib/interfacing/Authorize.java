package server.lib.interfacing;

import server.lib.model.DatabaseModel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Authorize {

    enum Method {
        CREATE, READ, UPDATE, DELETE;
    }

    Class<? extends DatabaseModel>[] permissions() default {};

    Method[] methods() default {};

    boolean important() default true;
}
