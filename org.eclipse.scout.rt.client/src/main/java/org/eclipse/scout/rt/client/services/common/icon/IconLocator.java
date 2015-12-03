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
package org.eclipse.scout.rt.client.services.common.icon;

import java.util.List;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.AbstractIcons;

/**
 * Get access to the current {@link IconLocator} using {@link BEANS#get(Class)}
 * <p>
 * or use the convenience {@link #instance()} method
 */
@ApplicationScoped
public class IconLocator {

  public static IconLocator instance() {
    return BEANS.get(IconLocator.class);
  }

  public IconSpec getIconSpec(String name) {
    if (name == null || AbstractIcons.Null.equals(name)) {
      return null;
    }
    List<IIconProviderService> iconProviderServices = BEANS.all(IIconProviderService.class);
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
