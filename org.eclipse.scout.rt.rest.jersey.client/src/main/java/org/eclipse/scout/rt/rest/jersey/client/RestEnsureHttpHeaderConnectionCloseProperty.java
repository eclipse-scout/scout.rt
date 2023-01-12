/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey.client;

import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;

/**
 * Ensures the HTTP header {@code Connection} is set to {@code close} for every REST call unless the header is already
 * set. Using Connection: close establishes a new connection for every REST call. This solves several rather rare issues
 * that occurred in the HTTP client implementation when a connection was closed by the server side while it was being
 * used by the client side for the next request. Default is true.
 *
 * @since 8.0
 */
public class RestEnsureHttpHeaderConnectionCloseProperty extends AbstractBooleanConfigProperty {

  @Override
  public Boolean getDefaultValue() {
    return true;
  }

  @Override
  public String description() {
    return "Ensures that the header 'Connection: close' is added to every REST HTTP requests unless the header 'Connection' is already set. "
        + "As a result, TCP Connections are not reused by consecutive REST calls."
        + "The default value is true\n"
        + "This solves several rather rare issues that occurred in the HTTP client implementation when a connection was closed by the server "
        + "side while it was being used by the client side for the next request.";
  }

  @Override
  public String getKey() {
    return "scout.rest.ensureHttpHeaderConnectionClose";
  }
}
