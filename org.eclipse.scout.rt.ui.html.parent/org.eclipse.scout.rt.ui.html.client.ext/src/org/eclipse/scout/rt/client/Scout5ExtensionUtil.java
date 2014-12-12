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

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;

// TODO AWE/CGU: (scout) siehe Desktop von Widget-App. Reflection Hack entfernen wenn
// in Scout erweitert
public final class Scout5ExtensionUtil {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(Scout5ExtensionUtil.class);

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
