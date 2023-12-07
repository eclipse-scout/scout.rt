/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.healthcheck;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.server.commons.ServerCommonsConfigProperties.RemoteHealthCheckUrlsProperty;
import org.eclipse.scout.rt.shared.http.DefaultHttpTransportManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;

/**
 * The <code>RemoteHealthChecker</code> provides a simple way to daisy-chain multiple applications' status (e.g. HTML UI
 * -> Server).
 *
 * @see RemoteHealthCheckUrlsProperty
 * @since 6.1
 */
public class RemoteHealthChecker extends AbstractHealthChecker {

  private static final Logger LOG = LoggerFactory.getLogger(RemoteHealthChecker.class);

  private final List<String> m_remoteUrls;

  public RemoteHealthChecker() {
    m_remoteUrls = BEANS.get(RemoteHealthCheckUrlsProperty.class).getValue();
  }

  @Override
  protected long getConfiguredTimeoutMillis() {
    return TimeUnit.SECONDS.toMillis(30);
  }

  @Override
  public boolean isActive() {
    return !CollectionUtility.isEmpty(m_remoteUrls);
  }

  @Override
  protected boolean execCheckHealth(HealthCheckCategoryId category) {
    return checkHealthInternal(category, false);
  }

  /**
   * Silently executes a remote health check, i.e. without logging in case of failure.
   */
  public boolean checkHealthSilent() {
    return checkHealthInternal(null, true);
  }

  protected boolean checkHealthInternal(HealthCheckCategoryId category, boolean silent) {
    if (m_remoteUrls != null) {
      for (String remote : m_remoteUrls) {
        GenericUrl remoteUrl = remote != null ? new GenericUrl(remote) : null;
        if (remoteUrl != null && category != null) {
          remoteUrl.put(HealthCheckServlet.QUERY_PARAMETER_NAME_CATEGORY, category.unwrap());
        }
        try {
          HttpRequest req = BEANS.get(DefaultHttpTransportManager.class).getHttpRequestFactory().buildHeadRequest(remoteUrl);
          req.getHeaders().setCacheControl("no-cache");
          HttpResponse resp = req.execute();
          int statusCode = resp.getStatusCode();
          if (statusCode < 200 || statusCode >= 400) {
            return false;
          }
        }
        catch (IOException e) {
          if (!silent || LOG.isDebugEnabled()) {
            //noinspection PlaceholderCountMatchesArgumentCount
            LOG.info("{} failed, message={}", getName(), e.getMessage(), LOG.isDebugEnabled() ? e : null);
          }
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public boolean acceptCategory(HealthCheckCategoryId category) {
    return Objects.equals(category, Readiness.ID);
  }
}
