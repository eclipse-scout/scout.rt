/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.ui.html.res.IWebContentService;
import org.json.JSONException;
import org.json.JSONObject;

public class DefaultValuesFilterService implements IDefaultValuesFilterService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DefaultValuesFilterService.class);

  private static final long FILE_UPDATE_CHECK_INTERVAL = 1234; // in milliseconds (only used in dev mode)

  private DefaultValuesFilter m_filter;
  private long m_lastCheckForFileUpdate; // timestamp in milliseconds (only used in dev mode)

  public DefaultValuesFilterService() {
  }

  protected DefaultValuesFilter getFilter() {
    return m_filter;
  }

  protected void setFilter(DefaultValuesFilter filter) {
    m_filter = filter;
  }

  @Override
  public void filter(JSONObject json) {
    try {
      ensureLoaded();
      m_filter.filter(json);
    }
    catch (RuntimeException e) {
      LOG.error("Error while filtering default values", e);
    }
  }

  @Override
  public void filter(JSONObject json, String objectType) {
    try {
      ensureLoaded();
      m_filter.filter(json, objectType);
    }
    catch (RuntimeException e) {
      LOG.error("Error while filtering default values", e);
    }
  }

  protected URL getDefaultValuesJsonUrl() {
    return BEANS.get(IWebContentService.class).getWebContentResource("res/defaultValues.json");
  }

  protected DefaultValuesFilter createDefaultValuesFilter(long lastModified, JSONObject defaultValuesConfig) throws JSONException {
    return new DefaultValuesFilter(lastModified, defaultValuesConfig.getJSONObject("defaults"), defaultValuesConfig.getJSONObject("objectTypeHierarchy"));
  }

  protected void ensureLoaded() {
    DefaultValuesFilter filter = m_filter;
    if (filter != null && Platform.get().inDevelopmentMode()) {
      long time = System.currentTimeMillis();
      if (time - m_lastCheckForFileUpdate > FILE_UPDATE_CHECK_INTERVAL) {
        m_lastCheckForFileUpdate = time;
        URL url = getDefaultValuesJsonUrl();
        try {
          URLConnection conn = url.openConnection();
          long lastModified = conn.getLastModified();
          if (lastModified != filter.lastModified()) {
            LOG.info("Detected modification in " + url);
            loadFilter();
          }
        }
        catch (Exception e) {
          LOG.warn("Error while checking for file modification of " + url, e);
        }
      }
    }

    if (m_filter == null) {
      loadFilter();
    }
  }

  protected synchronized void loadFilter() {
    try {
      URL url = getDefaultValuesJsonUrl();
      URLConnection conn = url.openConnection();
      long lastModified = conn.getLastModified();
      String jsonData = IOUtility.getContentUtf8(conn.getInputStream());
      jsonData = JsonUtility.stripCommentsFromJson(jsonData);
      JSONObject json = new JSONObject(jsonData);
      m_filter = createDefaultValuesFilter(lastModified, json);
    }
    catch (Exception e) {
      throw new ProcessingException("Unexpected error while initializing default values filter", e);
    }
  }
}
