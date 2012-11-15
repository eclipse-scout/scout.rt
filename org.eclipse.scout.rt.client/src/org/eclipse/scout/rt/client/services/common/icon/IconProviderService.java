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
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

/**
 * icon locator looking up bundle resources in a folder
 */
public class IconProviderService extends AbstractIconProviderService implements IIconProviderService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(IconProviderService.class);

  private Bundle m_hostBundle;

  @Override
  public void initializeService(ServiceRegistration registration) {
    super.initializeService(registration);
    setHostBundle(registration.getReference().getBundle());
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
  public IconSpec getIconSpec(String iconName) {
    if (m_hostBundle == null) {
      return null;
    }
    return super.getIconSpec(iconName);
  }

  @Override
  protected URL findResource(String fullPath) {
    URL[] entries = FileLocator.findEntries(m_hostBundle, new Path(fullPath));
    if (entries != null && entries.length > 0) {
      URL url = entries[entries.length - 1];
      if (LOG.isDebugEnabled()) {
        LOG.debug("find image " + fullPath + " in bundle " + m_hostBundle.getSymbolicName() + "->" + url);
      }
      return url;
    }
    return null;
  }
}
