/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jaxws.provider.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.server.jaxws.provider.context.RunContextProvider;

/**
 * Indicates to execute methods of a type on behalf of a {@link RunContext}.
 *
 * @since 5.1
 */
@Target(java.lang.annotation.ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RunWithRunContext {

  /**
   * The provider to be used to obtain a {@link RunContext}. By default, {@link RunContextProvider} is used.
   */
  Class<? extends RunContextProvider> provider() default RunContextProvider.class;
}
