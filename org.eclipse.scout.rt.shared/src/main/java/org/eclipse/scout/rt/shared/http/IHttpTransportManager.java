/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.http;

import org.eclipse.scout.rt.platform.ApplicationScoped;

import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;

/**
 * Interface to manager {@link HttpTransport} instances for other application classes.
 */
@ApplicationScoped
public interface IHttpTransportManager {

  /**
   * @return Technical transport manager name used for metrics.
   */
  String getName();

  /**
   * Get the {@link HttpTransport} instance. This method may create new instances or return a previously created one.
   */
  HttpTransport getHttpTransport();

  /**
   * Get the {@link HttpRequestFactory} for the specific {@link HttpTransport}.
   */
  HttpRequestFactory getHttpRequestFactory();

  /**
   * @param builder
   *          builder if available
   */
  void interceptNewHttpTransport(IHttpTransportBuilder builder);
}
