package org.springframework.jmx.metadata.support.annotations;

import java.lang.annotation.*;

/**
 * @author robh
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited()
public @interface ManagedResource {
    String objectName();
    String description() default "";
    int currencyTimeLimit() default 0;

    boolean log() default false;
    String logFile() default "";

    String persistPolicy() default "Never";
    int persistPeriod() default 0;
    String persistLocation() default "";
    String persistName() default "";
}
