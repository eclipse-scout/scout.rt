/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.IOUtility;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpIOExceptionHandler;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;

/**
 * <p>
 * Abstract implementation of an {@link IHttpTransportManager}. Empty subclasses can be created to use a default
 * {@link IHttpTransportManager} implementation bean.
 * </p>
 * <p>
 * Also acts a {@link IPlatformListener}, listening for platform change events. In case of a
 * {@link State#PlatformStopping} event, the underlying {@link HttpTransport} is shut down.
 * </p>
 */
public abstract class AbstractHttpTransportManager implements IHttpTransportManager, IPlatformListener {

  /**
   * Is {@link AbstractHttpTransportManager} still active?
   */
  private volatile boolean m_active = true;

  /**
   * Initialized?
   */
  private volatile boolean m_initialized = false;

  /**
   * Cached {@link HttpTransport}.
   */
  private volatile HttpTransport m_httpTransport;

  /**
   * Cached {@link HttpRequestFactory}.
   */
  private volatile HttpRequestFactory m_httpRequestFactory;

  /**
   * Cached {@link HttpRequestInitializer}.
   */
  private volatile HttpRequestInitializer m_httpRequestInitializer;

  @Override
  public HttpTransport getHttpTransport() {
    init();
    return m_httpTransport;
  }

  @Override
  public HttpRequestFactory getHttpRequestFactory() {
    init();
    return m_httpRequestFactory;
  }

  /**
   * Initialize the manager (if not initialized yet). Method call should be cheap as this method is called plenty of
   * times.
   */
  protected void init() {
    if (!m_initialized) {
      initSynchronized();
    }
  }

  /**
   * Create the {@link HttpTransport} (using factory), fill {@link #m_httpTransport} field.
   */
  protected synchronized void initSynchronized() {
    if (m_initialized || !m_active) {
      return;
    }

    m_httpRequestInitializer = createHttpRequestInitializer();
    m_httpTransport = createHttpTransport();
    m_httpRequestFactory = m_httpTransport.createRequestFactory(getHttpRequestInitializer());

    m_initialized = true;
  }

  /**
   * Default implementation of {@link HttpRequestInitializer} (see example for interface). We actually prefer to disable
   * the read timeouts and do not throw exceptions in case of non 2xx responses.
   */
  protected HttpRequestInitializer createHttpRequestInitializer() {
    return new DefaultHttpRequestInitializer();
  }

  protected HttpTransport createHttpTransport() {
    return BEANS.get(getHttpTransportFactory()).newHttpTransport(this);
  }

  @Override
  public void interceptNewHttpTransport(IHttpTransportBuilder builder) {
    // nop
  }

  /**
   * {@link IHttpTransportFactory} used to create the {@link HttpTransport}.
   */
  protected Class<? extends IHttpTransportFactory> getHttpTransportFactory() {
    return CONFIG.getPropertyValue(HttpTransportFactoryProperty.class);
  }

  /**
   * Possibility to specify a {@link HttpRequestInitializer} used for all requests.
   */
  protected HttpRequestInitializer getHttpRequestInitializer() {
    return m_httpRequestInitializer;
  }

  @Override
  public void stateChanged(PlatformEvent event) {
    if (event.getState() == State.PlatformStopping && m_httpTransport != null) {
      removeHttpTransport();
    }
  }

  /**
   * Remove {@link HttpTransport}.
   */
  protected synchronized void removeHttpTransport() {
    if (m_httpTransport == null) {
      return;
    }

    try {
      m_httpTransport.shutdown();
    }
    catch (IOException e) {
      throw new ProcessingException("Error during HttpTransport shut down.", e);
    }
    finally {
      m_active = false;
      m_httpTransport = null;
    }
  }

  public byte[] readFromUrl(URL url) throws IOException {
    GenericUrl genericUrl = new GenericUrl(url);
    HttpRequest req = getHttpRequestFactory().buildGetRequest(genericUrl);
    HttpResponse resp = req.execute();
    Long contentLength = resp.getHeaders().getContentLength();
    try (InputStream in = new BufferedInputStream(resp.getContent())) {
      return IOUtility.readBytes(in, contentLength != null ? contentLength.intValue() : -1);
    }
  }

  /**
   * Example {@link HttpRequestInitializer} (see {@link HttpRequestInitializer}) to disable read timeout and do not
   * throw exceptions in case of non 2xx responses.
   */
  public static class DefaultHttpRequestInitializer implements HttpRequestInitializer, HttpIOExceptionHandler {
    @Override
    public void initialize(HttpRequest request) throws IOException {
      // There may be requests that take longer than the default (20sec). Allow indefinite (similar to default UrlConnection behavior).
      request.setReadTimeout(0);

      // Do not throw an exception on execute error (similar to default UrlConnection behavior).
      request.setThrowExceptionOnExecuteError(false);

      // The request asks this handler in case a request failed; the supportsRetry flag is based on the HttpContent.retrySupported() callback flag.
      request.setIOExceptionHandler(this);
    }

    @Override
    public boolean handleIOException(HttpRequest request, boolean supportsRetry) {
      return true;
    }
  }
}
