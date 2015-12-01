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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.FileUtility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultValuesFilterService implements IDefaultValuesFilterService {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultValuesFilterService.class);

  private static final long FILE_UPDATE_CHECK_INTERVAL = 1234; // in milliseconds (only used in dev mode)

  private DefaultValuesFilter m_filter;
  private long m_lastModified = -1;
  private long m_lastCheckForFileUpdate = -1; // timestamp in milliseconds (only used in dev mode)
  private String m_combinedDefaultValuesConfiguration = null;

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

  protected List<URL> getDefaultValuesConfigurationUrls() {
    List<URL> urls = new ArrayList<>();
    for (IDefaultValuesConfigurationContributor contributor : BEANS.all(IDefaultValuesConfigurationContributor.class)) {
      URL url = contributor.contributeDefaultValuesConfigurationUrl();
      if (url != null) {
        urls.add(url);
      }
    }
    return urls;
  }

  protected void ensureLoaded() {
    DefaultValuesFilter filter = m_filter;
    if (filter != null && Platform.get().inDevelopmentMode()) {
      long time = System.currentTimeMillis();
      if (time - m_lastCheckForFileUpdate > FILE_UPDATE_CHECK_INTERVAL) {
        m_lastCheckForFileUpdate = time;
        List<URL> urls = getDefaultValuesConfigurationUrls();
        for (URL url : urls) {
          try {
            URLConnection conn = url.openConnection();
            long lastModified = conn.getLastModified();
            if (lastModified > m_lastModified) {
              LOG.info("Detected modification in " + url);
              loadFilter();
              break;
            }
          }
          catch (Exception e) {
            LOG.warn("Error while checking for file modification of " + url, e);
          }
        }
      }
    }

    if (m_filter == null) {
      loadFilter();
    }
  }

  protected synchronized void loadFilter() {
    try {
      // Build a list of default values configs
      List<URL> urls = getDefaultValuesConfigurationUrls();
      long newestModified = 0;
      List<JSONObject> defaultValuesConfigurations = new ArrayList<JSONObject>();
      for (URL url : urls) {
        URLConnection conn = url.openConnection();
        long lastModified = conn.getLastModified();
        if (lastModified > newestModified) {
          newestModified = lastModified;
        }
        String jsonData = IOUtility.getContentUtf8(conn.getInputStream());
        jsonData = JsonUtility.stripCommentsFromJson(jsonData);
        JSONObject json = new JSONObject(jsonData);
        defaultValuesConfigurations.add(json);
      }
      m_lastModified = newestModified;

      // Combine configs into "defaults" and "objectTypeHierarchy"
      JSONObject combinedDefaultValuesConfiguration = new JSONObject();
      for (JSONObject defaultValuesConfiguration : defaultValuesConfigurations) {
        JsonObjectUtility.mergeProperties(combinedDefaultValuesConfiguration, defaultValuesConfiguration);
      }

      // Build combined string (suitable to send to UI)
      m_combinedDefaultValuesConfiguration = JsonObjectUtility.toString(combinedDefaultValuesConfiguration);

      // Build filter
      DefaultValuesFilter filter = BEANS.get(DefaultValuesFilter.class);
      filter.importConfiguration(combinedDefaultValuesConfiguration);
      m_filter = filter;
    }
    catch (Exception e) {
      throw new ProcessingException("Unexpected error while initializing default values filter", e);
    }
  }

  @Override
  public synchronized String getCombinedDefaultValuesConfiguration() {
    ensureLoaded();
    return m_combinedDefaultValuesConfiguration;
  }

  @Override
  public synchronized BinaryResource getCombinedDefaultValuesConfigurationFile(String targetFilename) {
    ensureLoaded();
    byte[] content = (m_combinedDefaultValuesConfiguration == null ? null : m_combinedDefaultValuesConfiguration.getBytes(StandardCharsets.UTF_8));
    BinaryResource res = new BinaryResource(targetFilename, FileUtility.getContentTypeForExtension("json"), content, m_lastModified);
    return res;
  }
}
