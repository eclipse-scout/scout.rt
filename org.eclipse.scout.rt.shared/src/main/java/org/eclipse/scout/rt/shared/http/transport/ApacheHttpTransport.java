/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.http.transport;

import java.io.Closeable;
import java.io.IOException;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpOptions;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;

/**
 * <p>
 * Unfortunately com.google.api.client.http.apache.ApacheHttpTransport/com.google.api.client.http.apache.v2.ApacheHttpTransport does not support the Apache HTTP client
 * library for versions greater than 4.3/4.5.
 * </p>
 * <p>
 * This implementation of a {@link HttpTransport} works with Apache HttpClient 5.2.x.
 * </p>
 *
 * @see "<a href="https://github.com/googleapis/google-http-java-client/issues/1205">Update to Apache HttpClient 5. #1205</a>"
 */
public class ApacheHttpTransport extends HttpTransport {

  private final HttpClient m_httpClient;

  public ApacheHttpTransport(HttpClient httpClient) {
    m_httpClient = httpClient;
  }

  public HttpClient getHttpClient() {
    return m_httpClient;
  }

  @Override
  protected LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
    if (method == null) {
      return null;
    }
    HttpUriRequestBase req;
    if (HttpMethods.GET.equalsIgnoreCase(method)) {
      req = new HttpGet(url);
    }
    else if (HttpMethods.POST.equalsIgnoreCase(method)) {
      req = new HttpPost(url);
    }
    else if (HttpMethods.PUT.equalsIgnoreCase(method)) {
      req = new HttpPut(url);
    }
    else if (HttpMethods.DELETE.equalsIgnoreCase(method)) {
      req = new HttpDelete(url);
    }
    else if (HttpMethods.OPTIONS.equalsIgnoreCase(method)) {
      req = new HttpOptions(url);
    }
    else if (HttpMethods.HEAD.equalsIgnoreCase(method)) {
      req = new HttpHead(url);
    }
    else if (HttpMethods.PATCH.equalsIgnoreCase(method)) {
      req = new HttpPatch(url);
    }
    else {
      throw new UnsupportedOperationException("Unknown request method: " + method);
    }
    return createRequestInternal(req);
  }

  protected ApacheHttpRequest createRequestInternal(HttpUriRequestBase req) {
    return new ApacheHttpRequest(m_httpClient, req);
  }

  @Override
  public void shutdown() throws IOException {
    HttpClient httpClient = getHttpClient();
    if (httpClient instanceof Closeable) {
      ((Closeable) httpClient).close();
    }
  }
}
