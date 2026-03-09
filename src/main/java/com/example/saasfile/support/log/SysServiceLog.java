package com.example.saasfile.support.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SysServiceLog {

    String moduleName() default "";

    OperateTypeEnum operateType() default OperateTypeEnum.LOG_TYPE_LOOK;

    boolean onlyExceptions() default false;
}
