/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.exception;

import java.io.Serial;
import java.security.Permission;
import java.util.Optional;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.IProcessingStatus;
import org.eclipse.scout.rt.platform.exception.VetoException;

/**
 * Indicates that a resource requested by a client can not be accessed.
 * <p>
 * May be used for HTTP 403 - "Forbidden"
 */
public class AccessForbiddenException extends VetoException {
  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * Optional code representing the {@link Permission} that caused the access check to fail.
   * <p>
   * This code is shown in the error popup (e.g. "Code: XYZ") to inform the user which permission is missing.
   */
  private String m_permissionCode;

  public AccessForbiddenException() {
    super();
  }

  public AccessForbiddenException(String message, Object... args) {
    super(message, args);
  }

  public AccessForbiddenException(IProcessingStatus status) {
    super(status);
  }

  public String getPermissionCode() {
    return m_permissionCode;
  }

  public AccessForbiddenException withPermission(Permission permission) {
    withPermissionCode(Optional.ofNullable(permission)
        .map(p -> BEANS.optional(IPermissionCodeHelper.class)
            .map(helper -> helper.getPermissionCode(p))
            .orElse(p.getName()))
        .orElse(null));
    return this;
  }

  public AccessForbiddenException withPermissionCode(final String permissionCode) {
    m_permissionCode = permissionCode;
    return this;
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
