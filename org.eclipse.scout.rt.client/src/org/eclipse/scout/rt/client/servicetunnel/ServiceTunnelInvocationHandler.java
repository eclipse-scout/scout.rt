/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.servicetunnel;

import org.eclipse.scout.rt.servicetunnel.IServiceTunnel;

/**
 * @deprecated: use {@link org.eclipse.scout.rt.servicetunnel.ServiceTunnelInvocationHandler} instead
 *              Will be removed in the 5.0 Release
 */
@Deprecated
public class ServiceTunnelInvocationHandler extends org.eclipse.scout.rt.servicetunnel.ServiceTunnelInvocationHandler {

  public ServiceTunnelInvocationHandler(Class<?> serviceInterfaceClass, IServiceTunnel tunnel) {
    super(serviceInterfaceClass, tunnel);
  }
}
