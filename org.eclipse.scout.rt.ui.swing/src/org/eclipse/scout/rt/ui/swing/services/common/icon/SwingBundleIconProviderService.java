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
package org.eclipse.scout.rt.ui.swing.services.common.icon;

import java.net.URL;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.services.common.icon.IconProviderService;
import org.eclipse.scout.rt.client.services.common.icon.IconSpec;
import org.eclipse.scout.rt.ui.swing.Activator;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

public class SwingBundleIconProviderService extends IconProviderService {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingBundleIconProviderService.class);

  public static final String EXTENSION_POINT = Activator.PLUGIN_ID + ".scouticons";
  public static final String FOLDER_NAME = "resources/icons/internal";

  public SwingBundleIconProviderService() {
    setHostBundle(Activator.getDefault().getBundle());
    setFolderName(FOLDER_NAME);
  }

  @Override
  public void initializeService(ServiceRegistration registration) {
    super.initializeService(registration);
    setHostBundle(Activator.getDefault().getBundle());
    setFolderName(FOLDER_NAME);
  }

  @Override
  public IconSpec getIconSpec(String iconName) {
    IconSpec iconSpec = getContributedIconSpec(iconName);
    if (iconSpec == null) {
      iconSpec = super.getIconSpec(iconName);
    }
    return iconSpec;
  }

  private IconSpec getContributedIconSpec(String iconName) {
    // remove file format if given
    int index = iconName.lastIndexOf('.');
    if (index > 0) {
      iconName = iconName.substring(0, index);
    }

    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint xp = registry.getExtensionPoint(EXTENSION_POINT);

    for (IExtension extension : xp.getExtensions()) {
      for (IConfigurationElement element : extension.getConfigurationElements()) {
        String elementName = element.getName();

        String resourceName = null;
        // normal icon
        if (elementName.equalsIgnoreCase(iconName)) {
          resourceName = element.getAttribute("icon");
        }
        else if (iconName.startsWith(elementName)) {
          String state = iconName.substring(elementName.length());
          // hover icon
          if ("_mouse_over".equalsIgnoreCase(state) || "_rollover".equalsIgnoreCase(state)) {
            resourceName = element.getAttribute("icon_hover");
            if (resourceName == null) {
              resourceName = element.getAttribute("icon");
            }
          }
          // selected icon
          else if ("_active".equalsIgnoreCase(state) || "_pressed".equalsIgnoreCase(state) || "_selected".equalsIgnoreCase(state)) {
            resourceName = element.getAttribute("icon_selected");
            if (resourceName == null) {
              resourceName = element.getAttribute("icon");
            }
          }
          // disabled icon
          else if ("_disabled".equalsIgnoreCase(state)) {
            resourceName = element.getAttribute("icon_disabled");
            if (resourceName == null) {
              resourceName = element.getAttribute("icon");
            }
          }
        }

        if (resourceName == null) {
          continue;
        }
        Bundle bundle = Platform.getBundle(element.getContributor().getName());
        URL url = bundle.getResource(resourceName);
        if (url == null) {
          continue;
        }

        IconSpec iconSpec = new IconSpec();
        try {
          byte[] content = IOUtility.getContent(url.openStream(), true);
          if (content != null) {
            iconSpec.setContent(content);
          }
          iconSpec.setName(iconName);
          return iconSpec;
        }
        catch (Exception e) {
          LOG.error("could not read input stream from url '" + url + "'.", e);
        }
      }
    }
    return null;
  }
}
