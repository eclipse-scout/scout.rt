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
package org.eclipse.scout.rt.ui.html.json;

import java.net.URL;
import java.net.URLConnection;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.html.Activator;
import org.eclipse.scout.service.AbstractService;
import org.json.JSONException;
import org.json.JSONObject;

public class DefaultValuesFilterService extends AbstractService implements IDefaultValuesFilterService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DefaultValuesFilterService.class);

  private long m_configFileLastModified = 0; // used in dev mode to reload config if the file was changed
  private DefaultValuesFilter m_filter;

  public DefaultValuesFilterService() {
  }

  protected URL getDefaultValuesConfigUrl() {
    return Activator.getDefault().getWebContentResourceLocator().getWebContentResource("res/defaultValues.json");
  }

  protected synchronized void init() throws ProcessingException {
    try {
      URL url = getDefaultValuesConfigUrl();
      String jsonData = IOUtility.getContentUtf8(url.openStream());
      jsonData = JsonUtility.stripCommentsFromJson(jsonData);
      JSONObject json = new JSONObject(jsonData);
      m_filter = createFilter(json);
    }
    catch (Exception e) {
      throw new ProcessingException("Unexpected error while initializing default values filter", e);
    }
  }

  protected DefaultValuesFilter createFilter(JSONObject defaultValuesConfig) throws JSONException {
    return new DefaultValuesFilter(defaultValuesConfig.getJSONObject("defaults"), defaultValuesConfig.getJSONObject("objectTypeHierarchy"));
  }

  protected void ensureInitialized() throws ProcessingException {
    if (Platform.inDevelopmentMode()) {
      URL url = getDefaultValuesConfigUrl();
      try {
        URLConnection conn = url.openConnection();
        long lastModified = conn.getLastModified();
        if (lastModified != m_configFileLastModified) {
          LOG.info("Detected modification in " + url);
          m_configFileLastModified = lastModified;
          m_filter = null; // force reload
        }
      }
      catch (Exception e) {
        LOG.warn("Error while checking for file modification of " + url, e);
      }
    }

    if (m_filter == null) {
      init();
    }
  }

  protected DefaultValuesFilter getFilter() throws ProcessingException {
    ensureInitialized();
    return m_filter;
  }

  @Override
  public void filter(JSONObject json) {
    try {
      getFilter().filter(json);
    }
    catch (ProcessingException e) {
      LOG.error("Error while filtering default values", e);
    }
  }

  @Override
  public void filter(JSONObject json, String objectType) {
    try {
      getFilter().filter(json, objectType);
    }
    catch (ProcessingException e) {
      LOG.error("Error while filtering default values", e);
    }
  }
}
