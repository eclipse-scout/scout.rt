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

import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
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
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(IconLocator.class);

  private final IIconProviderService[] m_iconProviderServices;

  public IconLocator(IClientSession session) {
    Bundle clientBundle = session.getBundle();
    TreeSet<IIconProviderService> services = new TreeSet<IIconProviderService>(new P_ServiceComparator());
    IIconProviderService[] registeredServices = SERVICES.getServices(IIconProviderService.class);
    services.addAll(Arrays.asList(registeredServices));
    boolean containsClientBundle = false;
    for (IIconProviderService service : registeredServices) {
      if (CompareUtility.equals(service.getHostBundle(), clientBundle)) {
        containsClientBundle = true;
        break;
      }
    }
    if (!containsClientBundle) {
      services.add(new P_ClientIconProviderService(clientBundle));
    }
    m_iconProviderServices = services.toArray(new IIconProviderService[services.size()]);
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

  private class P_ServiceComparator implements Comparator<IIconProviderService> {
    @Override
    public int compare(IIconProviderService o1, IIconProviderService o2) {
      if (o1 == null) {
        return -1;
      }
      if (o2 == null) {
        return 1;
      }
      int result = o2.getRanking() - o1.getRanking();
      if (result == 0) {
        LOG.warn("Multiple IIconProviderServices with the same ranking found. A clear definition of the rankings is necessary to properly enable the icon overriding. Affected services: " + o1 + ", " + o2);
        return -1;
      }

      return result;
    }
  }

  private class P_ClientIconProviderService extends IconProviderService {

    public P_ClientIconProviderService(Bundle clientBundle) {
      setHostBundle(clientBundle);
    }

    @Override
    public int getRanking() {
      return -20;
    }
  }

}
