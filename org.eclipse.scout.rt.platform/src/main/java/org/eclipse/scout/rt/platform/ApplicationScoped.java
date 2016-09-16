/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A class annotated with this annotation represents a Bean which is application scoped, meaning that that Bean only
 * exists once per application, or classloader. All classes marked with this annotation (or an annotation that has this
 * annotation) are automatically registered in the {@link IBeanManager}.
 * <p>
 * It does not ensure that the bean is constructed only once. For example multiple instances might be created if the
 * bean is requested at almost the same time by multiple threads. However always the same instance will be used within
 * the application (all other instances are discarded after construction and never used within the application). <br>
 * For initialization code that needs to be run exactly once, create a method annotated with @PostConstruct.
 * </p>
 */
@Bean
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ApplicationScoped {

}
