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
package org.eclipse.scout.service.internal;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.scout.service.IServiceFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

class DirectServiceAccessor {
  private final IExtension m_owner;
  private final IServiceFactory m_factory;
  private final ServiceRegistration m_registration;

  DirectServiceAccessor(IExtension owner, IServiceFactory factory, ServiceRegistration registration) {
    m_owner = owner;
    m_factory = factory;
    m_registration = registration;
  }

  public IExtension getOwner() {
    return m_owner;
  }

  public Object getServiceImpl(BundleContext context) {
    return m_factory.getService(context.getBundle(), m_registration);
  }
}
