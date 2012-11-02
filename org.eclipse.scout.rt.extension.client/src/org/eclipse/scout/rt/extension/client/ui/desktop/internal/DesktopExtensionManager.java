/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.extension.client.ui.desktop.internal;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.desktop.IDesktopExtension;
import org.eclipse.scout.rt.extension.client.internal.AbstractExtensionManager;
import org.eclipse.scout.rt.extension.client.internal.IExtensionProcessor;
import org.osgi.framework.Bundle;

/**
 * @since 3.9.0
 */
public class DesktopExtensionManager extends AbstractExtensionManager {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DesktopExtensionManager.class);

  public static final String EXTENSION_POINT_ID = org.eclipse.scout.rt.extension.client.Activator.PLUGIN_ID + ".desktopExtensions";
  public static final String DESKTOP_EXTENSION_ELEMENT = "desktopExtension";

  private final List<Class<? extends IDesktopExtension>> m_desktopExtensionTypes;

  public DesktopExtensionManager(IExtensionRegistry extensionRegistry) {
    super(extensionRegistry, EXTENSION_POINT_ID);
    m_desktopExtensionTypes = new LinkedList<Class<? extends IDesktopExtension>>();
    initExtensionProcessors();
  }

  public List<IDesktopExtension> getDesktopExtensions() {
    synchronized (getLock()) {
      ensureStarted();
      List<IDesktopExtension> desktopExtensions = new ArrayList<IDesktopExtension>(m_desktopExtensionTypes.size());
      for (Class<? extends IDesktopExtension> type : m_desktopExtensionTypes) {
        try {
          IDesktopExtension desktopExtension = type.newInstance();
          desktopExtensions.add(desktopExtension);
        }
        catch (Exception e) {
          LOG.warn("Exception while instantiating new object of type [" + type.getName() + "]", e);
        }
      }
      return desktopExtensions;
    }
  }

  private void initExtensionProcessors() {
    addExtensionProcessor(DESKTOP_EXTENSION_ELEMENT,
        new IExtensionProcessor<Class<? extends IDesktopExtension>>() {
          @Override
          public Class<? extends IDesktopExtension> processConfigurationElement(Bundle contributor, IConfigurationElement element) throws Exception {
            Class<? extends IDesktopExtension> desktopExtension = loadClass(contributor, IDesktopExtension.class, element.getAttribute("class"));
            m_desktopExtensionTypes.add(desktopExtension);
            return desktopExtension;
          }
        });
  }

  @Override
  protected void removeContributions(Set<Object> contributions) {
    m_desktopExtensionTypes.removeAll(contributions);
  }
}
