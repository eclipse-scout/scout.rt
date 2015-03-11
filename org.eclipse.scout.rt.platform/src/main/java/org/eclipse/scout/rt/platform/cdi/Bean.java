package org.eclipse.scout.rt.platform.cdi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.commons.annotations.Priority;

/**
 * All objects marked with this annotation (or an annotation that has this annotation) are automatically registered in
 * the scout {@link IBeanContext}
 * <p>
 * see also {@link Priority}, {@link ApplicationScoped}, and annotations qualifed with {@link BeanInvocationHint}
 * <p>
 * In more details...
 * <p>
 * The existence of the file
 * <code>src/main/resources/META-INF/services/org.eclipse.scout.rt.platform.cdi.IBeanContributor</code> will make scout
 * add all relevant beans in this maven module with {@link IBeanContext#registerClass(Class)}. <br/>
 * These includes classes that satisfy all of the following rules:
 * <ol>
 * <li>class is public or protected</li>
 * <li>class is top level or static inner type</li>
 * <li>class has annotation {@link Bean} or an annotation that itself has the qualifier {@link Bean} (such as
 * {@link ApplicationScoped})</li>
 * </ol>
 * that have a {@link Bean} annotation
 * <p>
 * All implemented classes of this interface are listed in a file
 * <code>src/main/resources/META-INF/services/org.eclipse.scout.rt.platform.cdi.IBeanContributor</code> and may manually
 * register custom beans.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Documented
@Inherited
public @interface Bean {
}
