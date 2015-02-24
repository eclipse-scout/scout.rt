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

import org.eclipse.scout.rt.client.services.common.icon.IIconProviderService;
import org.eclipse.scout.rt.client.services.common.icon.IconSpec;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.service.SERVICES;

public class IconLocator implements IIconLocator {

  @Override
  public IconSpec getIconSpec(String name) {
    if (name == null || AbstractIcons.Null.equals(name)) {
      return null;
    }

    List<IIconProviderService> iconProviderServices = SERVICES.getServices(IIconProviderService.class);
    IconSpec spec = null;
    for (IIconProviderService service : iconProviderServices) {
      spec = service.getIconSpec(name);
      if (spec != null) {
        break;
      }
    }
    return spec;
  }
}
