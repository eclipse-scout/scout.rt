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
