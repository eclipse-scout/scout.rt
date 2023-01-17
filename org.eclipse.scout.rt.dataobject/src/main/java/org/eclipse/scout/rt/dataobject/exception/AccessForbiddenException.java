/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.exception;

import org.eclipse.scout.rt.platform.exception.IProcessingStatus;
import org.eclipse.scout.rt.platform.exception.VetoException;

/**
 * Indicates that a resource requested by a client can not be accessed.
 * <p>
 * May be used for HTTP 403 - "Forbidden"
 */
public class AccessForbiddenException extends VetoException {
  private static final long serialVersionUID = 1L;

  public AccessForbiddenException() {
    super();
  }

  public AccessForbiddenException(String message, Object... args) {
    super(message, args);
  }

  public AccessForbiddenException(IProcessingStatus status) {
    super(status);
  }

  @Override
  public AccessForbiddenException withContextInfo(final String name, final Object value, final Object... valueArgs) {
    super.withContextInfo(name, value, valueArgs);
    return this;
  }

  @Override
  public AccessForbiddenException withTitle(final String title, final Object... args) {
    super.withTitle(title, args);
    return this;
  }

  @Override
  public AccessForbiddenException withCode(final int code) {
    super.withCode(code);
    return this;
  }

  @Override
  public AccessForbiddenException withSeverity(final int severity) {
    super.withSeverity(severity);
    return this;
  }

  @Override
  public AccessForbiddenException withStatus(final IProcessingStatus status) {
    super.withStatus(status);
    return this;
  }
}
