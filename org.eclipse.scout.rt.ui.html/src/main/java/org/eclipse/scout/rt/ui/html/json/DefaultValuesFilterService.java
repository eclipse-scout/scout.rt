/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.util.FileUtility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheControl;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultValuesFilterService implements IDefaultValuesFilterService {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultValuesFilterService.class);

  private static final long FILE_UPDATE_CHECK_INTERVAL = 1234; // in milliseconds (only used in dev mode)

  private DefaultValuesFilter m_filter;
  private volatile long m_lastModified = -1;
  private long m_lastCheckForFileUpdate = -1; // timestamp in milliseconds (only used in dev mode)
  private String m_combinedDefaultValuesConfiguration = null;

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
    List<IDefaultValuesConfigurationContributor> contributors = BEANS.all(IDefaultValuesConfigurationContributor.class);
    Collections.reverse(contributors);
    for (IDefaultValuesConfigurationContributor contributor : contributors) {
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
              LOG.info("Detected modification in {}", url);
              loadFilter();
              break;
            }
          }
          catch (Exception e) {
            LOG.warn("Error while checking for file modification of {}", url, e);
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
      List<JSONObject> defaultValuesConfigurations = new ArrayList<>();
      for (URL url : urls) {
        URLConnection conn = url.openConnection();
        long lastModified = conn.getLastModified();
        if (lastModified > newestModified) {
          newestModified = lastModified;
        }
        String jsonData;
        try (InputStream in = conn.getInputStream()) {
          jsonData = IOUtility.readStringUTF8(in);
        }
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
    return BinaryResources.create()
        .withFilename(targetFilename)
        .withContentType(FileUtility.getContentTypeForExtension("json"))
        .withContent(content)
        .withLastModified(m_lastModified)
        .withCachingAllowed(true)
        .withCacheMaxAge(HttpCacheControl.MAX_AGE_NONE)
        .build();
  }
}
