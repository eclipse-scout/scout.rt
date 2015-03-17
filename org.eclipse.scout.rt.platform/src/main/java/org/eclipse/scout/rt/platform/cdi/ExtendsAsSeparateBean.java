package org.eclipse.scout.rt.platform.cdi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.commons.annotations.Priority;

/**
 * Typically when a bean B extends A (with a higer {@link Priority}) then the semantics is that B is replacing A in the
 * {@link IBeanContext}
 * <p>
 * {@link OBJ#get(Class)} with A.class will yield an instance of B.
 * <p>
 * However, sometimes it is not intended that B is replacing A in the {@link IBeanContext}, it even has the same
 * {@link Priority}.
 * <p>
 * {@link OBJ#get(Class)} with A.class should yield an instance of A whereas {@link OBJ#get(Class)} with B.class should
 * yield an instance of B.
 * <p>
 * This latter use case is solved by annotating B with {@link ExtendsAsSeparateBean}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Documented
public @interface ExtendsAsSeparateBean {
}
