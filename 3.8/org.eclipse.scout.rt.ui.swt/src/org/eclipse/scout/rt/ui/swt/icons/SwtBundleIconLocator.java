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
package org.eclipse.scout.rt.ui.swt.icons;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.services.common.icon.IconProviderService;
import org.eclipse.scout.rt.client.services.common.icon.IconSpec;
import org.eclipse.scout.rt.client.ui.IIconLocator;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.ui.swt.services.common.icon.SwtBundleIconProviderService;

public class SwtBundleIconLocator implements IIconLocator {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtBundleIconLocator.class);

  private final IconProviderService m_swtIconProviderService;

  public SwtBundleIconLocator() {
    m_swtIconProviderService = new SwtBundleIconProviderService();
  }

  @Override
  public IconSpec getIconSpec(String name) {
    if (name == null || AbstractIcons.Null.equals(name)) {
      return null;
    }
    return m_swtIconProviderService.getIconSpec(name);
  }
}
