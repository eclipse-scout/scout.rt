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
package org.eclipse.scout.rt.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;

// TODO AWE/CGU: (scout) siehe Desktop von Widget-App. Reflection Hack entfernen wenn
// in Scout erweitert
public final class Scout5ExtensionUtil {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(Scout5ExtensionUtil.class);

  public static void ISession_initCustomParams(IClientSession clientSession, Map<String, String> customParams) {
    try {
      Method method = clientSession.getClass().getMethod("initCustomParams", Map.class);
      method.invoke(clientSession, customParams);
    }
    catch (Throwable e) {
      LOG.warn("method initCustomParams does not exist in " + clientSession);
    }
  }

  @SuppressWarnings("unchecked")
  public static List<Object> IDesktop_getAddOns(IDesktop desktop) {
    try {
      Method method = desktop.getClass().getMethod("getAddOns");
      return (List<Object>) method.invoke(desktop);
    }
    catch (Throwable e) {
      LOG.warn("method getAddOns does not exist in " + desktop);
      return Collections.emptyList();
    }
  }

  public static <T> T IDesktop_getAddOn(IDesktop desktop, Class<T> addOnInterface) {
    for (Object addOn : IDesktop_getAddOns(desktop)) {
      if (addOnInterface.isInstance(addOn)) {
        return addOnInterface.cast(addOn);
      }
    }
    return null;
  }

  public static boolean IDesktop_isOutlineChanging(IDesktop desktop) {
    Field outlineChangingField;
    try {
      outlineChangingField = AbstractDesktop.class.getDeclaredField("m_outlineChanging");
      outlineChangingField.setAccessible(true);
      return (boolean) outlineChangingField.get(desktop);
    }
    catch (Exception e) {
      LOG.warn("", e);
      return false;
    }
  }

}
