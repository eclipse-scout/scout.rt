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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes marked with this annotation are not automatically registered in scout's {@link IBeanManager}. The annotation
 * may also be used by any other party that modifies the {@link IBeanManager} (e.g. by an {@link IPlatformListener}).
 * <p>
 * Other than the {@link Bean} annotation this one is not inherited by subclasses, implementing classes or annotated
 * classes. In other words: this annotation affects only the classes it has been added to directly.
 *
 * @since 5.1
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreBean {

}
