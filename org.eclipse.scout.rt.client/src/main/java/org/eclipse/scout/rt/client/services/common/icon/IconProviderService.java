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
package org.eclipse.scout.rt.client.services.common.icon;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

/**
 * icon locator looking up bundle resources in a folder
 */
public class IconProviderService extends AbstractIconProviderService implements IIconProviderService {

  private Bundle m_hostBundle;

  @Override
  public void initializeService() {
    super.initializeService();
    // TODO NOOSGI
//    if (serviceRef instanceof IOsgiServiceReference) {
//      setHostBundle(((IOsgiServiceReference) serviceRef).getBundle());
//    }
//    else {
//      throw new IllegalArgumentException("TODO implement the no OSGI way.");
//    }
  }

  @Override
  public Bundle getHostBundle() {
    return m_hostBundle;
  }

  public void setHostBundle(Bundle bundle) {
    URL[] iconEntries = FileLocator.findEntries(bundle, new Path(getFolderName()));
    if (iconEntries != null && iconEntries.length > 0) {
      m_hostBundle = bundle;
    }
  }

  @Override
  protected URL findResource(String fullPath) {
    return getClass().getClassLoader().getResource(fullPath);
  }
}
