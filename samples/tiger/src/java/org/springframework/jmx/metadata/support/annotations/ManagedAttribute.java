package org.springframework.jmx.metadata.support.annotations;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Inherited;

/**
 * @author robh
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ManagedAttribute {
    String defaultValue() default "";
    String description() default "";
    int currencyTimeLimit() default 0;
    String persistPolicy() default "Never";
    int persistPeriod() default 0;
}
