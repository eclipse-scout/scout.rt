/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.platform.internal.SingeltonBeanInstanceProducer;

/**
 * A class annotated with this annotation represents a Bean which is application scoped, meaning that that Bean only
 * exists once per application, or classloader. All classes marked with this annotation (or an annotation that has this
 * annotation) are automatically registered in the {@link IBeanManager}.
 * <p>
 * It does not necessarily ensure that the bean is constructed only once. For example multiple instances might be
 * created if the bean is requested at almost the same time by multiple threads. However always the same instance will
 * be used within the application (all other instances are discarded after construction and never used within the
 * application). For initialization code that needs to be run exactly once, create a method annotated
 * with @PostConstruct. Details are documented on the {@link IBeanInstanceProducer} used when the bean is registered, by
 * default {@link SingeltonBeanInstanceProducer}.
 *
 * @see SingeltonBeanInstanceProducer
 */
@Bean
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ApplicationScoped {

}
