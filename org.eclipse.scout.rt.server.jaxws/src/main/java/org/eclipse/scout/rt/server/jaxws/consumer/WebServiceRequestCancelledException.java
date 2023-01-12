/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jaxws.consumer;

import org.eclipse.scout.rt.platform.exception.PlatformException;

/**
 * Indicates that the result of a web service request cannot be retrieved because the request was cancelled.
 *
 * @since 5.2
 */
public class WebServiceRequestCancelledException extends PlatformException {

  private static final long serialVersionUID = 1L;

  /**
   * See constructor of {@link PlatformException}
   */
  public WebServiceRequestCancelledException(final String message, final Object... args) {
    super(message, args);
  }

  @Override
  public WebServiceRequestCancelledException withContextInfo(final String name, final Object value, final Object... valueArgs) {
    super.withContextInfo(name, value, valueArgs);
    return this;
  }
}
