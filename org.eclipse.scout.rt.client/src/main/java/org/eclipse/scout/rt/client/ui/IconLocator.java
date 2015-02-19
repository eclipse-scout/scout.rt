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
package org.eclipse.scout.rt.client.ui;

import java.util.List;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.icon.IIconProviderService;
import org.eclipse.scout.rt.client.services.common.icon.IconProviderService;
import org.eclipse.scout.rt.client.services.common.icon.IconSpec;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.service.SERVICES;
import org.osgi.framework.Bundle;

/**
 *
 */
public class IconLocator implements IIconLocator {

  private final List<IIconProviderService> m_iconProviderServices;

  public IconLocator(IClientSession session) {
    Bundle clientBundle = session.getBundle();
    List<IIconProviderService> registeredServices = SERVICES.getServices(IIconProviderService.class);
    boolean containsClientBundle = false;
    for (IIconProviderService service : registeredServices) {
      if (CompareUtility.equals(service.getHostBundle(), clientBundle)) {
        containsClientBundle = true;
        break;
      }
    }
    if (!containsClientBundle) {
      registeredServices.add(new P_ClientIconProviderService(clientBundle));
    }
    m_iconProviderServices = registeredServices;
  }

  @Override
  public IconSpec getIconSpec(String name) {
    if (name == null || AbstractIcons.Null.equals(name)) {
      return null;
    }
    IconSpec spec = null;
    for (IIconProviderService service : m_iconProviderServices) {
      spec = service.getIconSpec(name);
      if (spec != null) {
        break;
      }
    }
    return spec;
  }

  private class P_ClientIconProviderService extends IconProviderService {

    public P_ClientIconProviderService(Bundle clientBundle) {
      setHostBundle(clientBundle);
    }

  }

}
