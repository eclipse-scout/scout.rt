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
package org.eclipse.scout.rt.shared.servicetunnel;

import java.lang.reflect.Method;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;

/**
 * Interface of a tunnel used to invoke a service through HTTP.
 */
@Bean
public interface IServiceTunnel {

  /**
   * The service tunnel is accessible over the {@link BEANS}. The service tunnel will always be available and indicates
   * readiness for usage with the active {@link IServiceTunnel#isActive()} method.
   *
   * @return true when the service tunnel is ready to get invoked false otherwise.
   */
  boolean isActive();

  /**
   * Invoke a remote service through a service tunnel<br>
   * The argument array may contain IHolder values which are updated as OUT parameters when the backend call has
   * completed flags are custom flags not used by the framework itself
   */
  Object invokeService(Class<?> serviceInterfaceClass, Method operation, Object[] args);
}
