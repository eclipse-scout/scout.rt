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

import org.eclipse.scout.rt.client.services.common.icon.IconProviderService;
import org.eclipse.scout.rt.client.services.common.icon.IconSpec;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.osgi.framework.Bundle;

/**
 *
 */
public class BundleIconLocator implements IIconLocator {

  private final IconProviderService m_iconLocatorService;

  public BundleIconLocator(Bundle bundle) {
    m_iconLocatorService = new P_BundleIconProviderService(bundle);
  }

  @Override
  public IconSpec getIconSpec(String name) {
    if (name == null || AbstractIcons.Null.equals(name)) {
      return null;
    }
    IconSpec spec = m_iconLocatorService.getIconSpec(name);
    return spec;
  }

  public IconProviderService getIconLocatorService() {
    return m_iconLocatorService;
  }

  private class P_BundleIconProviderService extends IconProviderService {

    public P_BundleIconProviderService(Bundle bundle) {
      setHostBundle(bundle);
    }

  }

}
