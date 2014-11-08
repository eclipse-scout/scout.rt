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
package org.eclipse.scout.rt.ui.html;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;

// TODO AWE/CGU: (scout) siehe Desktop von Widget-App. Reflection Hack entfernen wenn
// in Scout erweitert
public final class Desktop5Util {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(Desktop5Util.class);

  @SuppressWarnings("unchecked")
  public static List<Object> getAddOns(IDesktop desktop) {
    try {
      Method method = desktop.getClass().getDeclaredMethod("getAddOns");
      return (List<Object>) method.invoke(desktop);
    }
    catch (ReflectiveOperationException e) {
      LOG.warn("method getAddOns does not exist in " + desktop);
      return Collections.emptyList();
    }

  }

  public static <T> T getAddOn(IDesktop desktop, Class<T> addOnInterface) {
    for (Object addOn : getAddOns(desktop)) {
      if (addOnInterface.isInstance(addOn)) {
        return addOnInterface.cast(addOn);
      }
    }
    return null;
  }

}
