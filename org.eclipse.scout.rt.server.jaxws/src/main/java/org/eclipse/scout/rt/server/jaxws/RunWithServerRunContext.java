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
package org.eclipse.scout.rt.server.jaxws;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.server.context.ServerRunContext;

/**
 * Indicates to execute methods of a type on behalf of a {@link ServerRunContext}.
 *
 * @since 5.1
 */
@Target(java.lang.annotation.ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RunWithServerRunContext {

  /**
   * The provider to be used to obtain a {@link ServerRunContext}. By default, {@link ServerRunContextProvider} is used.
   */
  Class<? extends ServerRunContextProvider> provider() default ServerRunContextProvider.class;
}
