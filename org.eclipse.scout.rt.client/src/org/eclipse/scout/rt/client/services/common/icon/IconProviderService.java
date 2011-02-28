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
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.service.AbstractService;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

/**
 *
 */
public class IconProviderService extends AbstractService implements IIconProviderService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(IconProviderService.class);

  private String m_folderName = "resources/icons";
  private String m_iconExtensions = "png,gif,jpg";
  private Bundle m_hostBundle;
  private String[] m_iconExtensionsArray;
  private int m_ranking;

  @Override
  public void initializeService(ServiceRegistration registration) {
    super.initializeService(registration);
    Object rankingProp = registration.getReference().getProperty(Constants.SERVICE_RANKING);
    if (rankingProp instanceof Integer) {
      m_ranking = ((Integer) rankingProp).intValue();
    }
    setHostBundle(registration.getReference().getBundle());
  }

  public void setHostBundle(Bundle bundle) {
    URL[] iconEntries = FileLocator.findEntries(bundle, new Path(getFolderName()));
    if (iconEntries != null && iconEntries.length > 0) {
      m_hostBundle = bundle;
    }

    ArrayList<String> fileExtensions = new ArrayList<String>();
    if (getIconExtensions() != null) {
      StringTokenizer tokenizer = new StringTokenizer(getIconExtensions(), ",;");
      while (tokenizer.hasMoreTokens()) {
        String t = tokenizer.nextToken().trim();
        if (t.length() > 0) {
          fileExtensions.add(t);
        }
      }
    }
    m_iconExtensionsArray = fileExtensions.toArray(new String[fileExtensions.size()]);
  }

  public int getRanking() {
    return m_ranking;
  }

  public Bundle getHostBundle() {
    return m_hostBundle;
  }

  protected String[] getIconExtensionsArray() {
    return m_iconExtensionsArray;
  }

  public IconSpec getIconSpec(String iconName) {
    if (m_hostBundle == null) {
      return null;
    }
    String name = iconName;
    if (StringUtility.isNullOrEmpty(name)) {
      return null;
    }
    name = name.replaceAll("\\A[\\/\\\\]*", "");
    if (!name.startsWith(getFolderName())) {
      name = getFolderName() + "/" + iconName;
    }
    String[] fqns = new String[getIconExtensionsArray().length + 1];
    String[] iconNames = new String[getIconExtensionsArray().length + 1];
    fqns[0] = name;
    iconNames[0] = iconName;
    for (int i = 1; i < fqns.length; i++) {
      fqns[i] = name + "." + getIconExtensionsArray()[i - 1];
      iconNames[i] = iconName + "." + getIconExtensionsArray()[i - 1];
    }

    IconSpec spec = null;
    spec = findIconSpec(m_hostBundle, fqns, iconNames);
    return spec;

  }

  public IconSpec findIconSpec(Bundle bundle, String[] fqns, String[] iconNames) {
    if (fqns != null && fqns.length > 0) {
      for (int i = 0; i < fqns.length; i++) {
        String fqn = fqns[i];
        String iconName = "";
        if (iconNames != null && iconNames.length > i) {
          iconName = iconNames[i];
        }
        URL[] entries = FileLocator.findEntries(bundle, new Path(fqn));
        if (entries != null && entries.length > 0) {
          URL url = entries[entries.length - 1];
          try {
            IconSpec iconSpec = new IconSpec();
            byte[] content = IOUtility.getContent(url.openStream(), true);
            if (content != null) {
              if (LOG.isDebugEnabled()) {
                LOG.debug("find image " + fqn + " in bundle " + bundle.getSymbolicName() + "->" + url);
              }
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
    }
    return null;
  }

  public void setFolderName(String folderName) {
    m_folderName = folderName;
  }

  public String getFolderName() {
    return m_folderName;
  }

  public void setIconExtensions(String iconExtensions) {
    m_iconExtensions = iconExtensions;
  }

  /**
   * @return a comma separated list of all extensions e.g. 'gif,png,jpg'
   */
  public String getIconExtensions() {
    return m_iconExtensions;
  }

}
