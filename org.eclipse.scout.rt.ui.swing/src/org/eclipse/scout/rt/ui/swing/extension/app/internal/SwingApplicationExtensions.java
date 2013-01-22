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
package org.eclipse.scout.rt.ui.swing.extension.app.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.ui.swing.Activator;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.extension.app.ISwingApplicationExtension;

/**
 * This class manages the contributed swing application extensions (see extension-point
 * org.eclipse.scout.rt.ui.swing.appextensions).
 * 
 * @author awe
 */
public class SwingApplicationExtensions implements ISwingApplicationExtensions {

  public static final String EXTENSION_POINT = Activator.PLUGIN_ID + ".appextensions";

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingApplicationExtensions.class);

  private Map<String, ISwingApplicationExtension> m_swingAppExtensionsMap = new HashMap<String, ISwingApplicationExtension>();

  private ISwingApplicationExtension m_activeSwingAppExtension;

  @Override
  public void start() {
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint xp = registry.getExtensionPoint(EXTENSION_POINT);
    int greatestRanking = 0;
    for (IExtension extension : xp.getExtensions()) {
      for (IConfigurationElement element : extension.getConfigurationElements()) {
        try {
          ISwingApplicationExtension swingAppExtension = (ISwingApplicationExtension) element.createExecutableExtension("class");
          int ranking = 0;
          String rankingString = element.getAttribute("ranking");
          if (rankingString != null) {
            ranking = Integer.parseInt(rankingString);
          }
          if (ranking >= greatestRanking) {
            greatestRanking = ranking;
            m_activeSwingAppExtension = swingAppExtension;
          }
          String extensionId = swingAppExtension.getExtensionId();
          if (m_swingAppExtensionsMap.containsKey(extensionId)) {
            LOG.error("Already registred a swing application extension with extensionId=" + extensionId + ". ExtensionId must be unique");
          }
          else {
            swingAppExtension.start();
            m_swingAppExtensionsMap.put(extensionId, swingAppExtension);
            LOG.debug("Added swing application extension " + swingAppExtension);
          }
        }
        catch (CoreException e) {
          LOG.error("failed to create swing application extension instance. element=" + element, e);
        }
      }
    }
    if (m_swingAppExtensionsMap.isEmpty()) {
      throw new IllegalStateException("no swing application extension contributed! at least one extension is required");
      // TODO AWE: (swingAppExt) können/sollen wir hier einfach einen default hinzufügen, wenn die map leer ist?
      // von der änderung betroffen ist ja auch der code der von der SDK generiert wird. Das mit einem scout-core
      // entwickler anschauen
    }
  }

  @Override
  public ISwingEnvironment getEnvironment() {
    return m_activeSwingAppExtension.getEnvironment();
  }

  @Override
  public IClientSession getClientSession() {
    return m_activeSwingAppExtension.getClientSession();
  }

  @Override
  public void showGUI() { // TODO AWE: (swingAppExt) checken
    for (ISwingApplicationExtension ext : m_swingAppExtensionsMap.values()) {
      ext.getEnvironment().showGUI(ext.getClientSession());
    }
  }

  @Override
  public boolean startUpSuccessful() {
    return true; // TODO AWE: (swingAppExt) impl.
  }

  @Override
  public Throwable getStartUpError() {
    return null; // TODO AWE: (swingAppExt) impl.
  }

  @Override
  public void stop() {
    for (ISwingApplicationExtension ext : m_swingAppExtensionsMap.values()) {
      ext.getClientSession().stopSession();
    }
  }

}
