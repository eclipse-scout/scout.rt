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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.server.commons.ServerCommonsConfigProperties.RemoteHealthCheckUrlsProperty;
import org.eclipse.scout.rt.shared.http.DefaultHttpTransportManager;

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
  protected boolean execCheckHealth(HealthCheckCategoryId category) throws Exception {
    boolean status = true;
    if (m_remoteUrls != null) {
      for (String remote : m_remoteUrls) {
        GenericUrl remoteUrl = remote != null ? new GenericUrl(remote) : null;
        if (remoteUrl != null && category != null) {
          remoteUrl.put(AbstractHealthCheckServlet.QUERY_PARAMETER_NAME_CATEGORY, category.unwrap());
        }
        HttpRequest req = BEANS.get(DefaultHttpTransportManager.class).getHttpRequestFactory().buildHeadRequest(remoteUrl);
        req.getHeaders().setCacheControl("no-cache");
        HttpResponse resp = req.execute();
        int statusCode = resp.getStatusCode();
        if (statusCode < 200 || statusCode >= 400) {
          status = false;
          break;
        }
      }
    }
    return status;
  }

  @Override
  public boolean acceptCategory(HealthCheckCategoryId category) {
    return Objects.equals(category, Readiness.ID);
  }
}
