package com.github.kevelbreh.journey;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

@Retention(CLASS) @Target(TYPE)
public @interface Route {
  String value() default "";
  String provider() default "default";
}
