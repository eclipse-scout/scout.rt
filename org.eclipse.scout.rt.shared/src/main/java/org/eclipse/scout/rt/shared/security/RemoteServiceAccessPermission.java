/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.security;

import java.util.regex.Pattern;

import org.eclipse.scout.rt.api.data.security.PermissionId;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.security.AbstractPermission;
import org.eclipse.scout.rt.security.IPermission;
import org.eclipse.scout.rt.security.IPermissionCollection;

/**
 * Permission to grant remote access to a service interface. It is checked in scouts ServiceOperationInvoker.
 */
public class RemoteServiceAccessPermission extends AbstractPermission {
  private static final long serialVersionUID = 1L;
  public static final PermissionId ID = PermissionId.of("scout.remote.service.access");

  private final String m_serviceOperation;
  private transient Pattern m_pattern;

  public RemoteServiceAccessPermission() {
    this(null);
  }

  /**
   * Permission granting access to remote service call
   * <p>
   * pattern may contain multiple * as wildcards
   */
  public RemoteServiceAccessPermission(String interfaceTypeName, String methodName) {
    this(interfaceTypeName.replace('$', '.') + "#" + methodName);
  }

  protected RemoteServiceAccessPermission(String serviceOperation) {
    super(ID);
    m_serviceOperation = serviceOperation;
  }

  public String getServiceOperation() {
    return m_serviceOperation;
  }

  public String getServiceOperationPattern() {
    return m_serviceOperation.replace(".", "[.]").replace("*", ".*");
  }

  @Override
  protected boolean evalPermission(IPermission p) {
    if (m_pattern == null) {
      m_pattern = Pattern.compile(getServiceOperationPattern());
    }
    RemoteServiceAccessPermission other = (RemoteServiceAccessPermission) p;
    if (other.getServiceOperation() == null) {
      return false;
    }
    return m_pattern.matcher(other.getServiceOperation()).matches();
  }

  @Override
  protected void validate(IPermissionCollection permissionCollection) {
    super.validate(permissionCollection);
    validateServiceOperationPattern();
  }

  protected void validateServiceOperationPattern() {
    Assertions.assertNotNull(getServiceOperation(), "Service operation pattern must not be null when assigned to a permission collection {}", this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((m_serviceOperation == null) ? 0 : m_serviceOperation.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    RemoteServiceAccessPermission other = (RemoteServiceAccessPermission) obj;
    if (m_serviceOperation == null) {
      if (other.m_serviceOperation != null) {
        return false;
      }
    }
    else if (!m_serviceOperation.equals(other.m_serviceOperation)) {
      return false;
    }
    return true;
  }
}
