/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
