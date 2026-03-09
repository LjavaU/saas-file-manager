package com.example.saasfile.support.schedule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Job {

    String jobDesc() default "";

    ScheduleTypeEnum scheduleType() default ScheduleTypeEnum.FIX_RATE;

    String scheduleConf() default "";

    String alarmEmail() default "";
}
