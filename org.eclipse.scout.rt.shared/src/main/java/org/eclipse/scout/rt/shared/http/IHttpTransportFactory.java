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

import com.google.api.client.http.HttpTransport;

/**
 * Factory (helper) for {@link IHttpTransportManager} (especially {@link AbstractHttpTransportManager}) classes to
 * create new instances of {@link HttpTransport}.
 */
@FunctionalInterface
@ApplicationScoped
public interface IHttpTransportFactory {

  /**
   * Create a new {@link HttpTransport} for the specific {@link IHttpTransportManager}.
   */
  HttpTransport newHttpTransport(IHttpTransportManager manager);
}
