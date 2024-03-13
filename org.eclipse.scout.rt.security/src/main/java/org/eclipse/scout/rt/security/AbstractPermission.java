/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.security;

import java.security.Permission;

import org.eclipse.scout.rt.api.data.security.PermissionId;
import org.eclipse.scout.rt.dataobject.id.IIds;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.Assertions;

public abstract class AbstractPermission extends Permission implements IPermission {
  private static final long serialVersionUID = 1L;

  /**
   * The following properties are effective immutable and protected by {@link #assertInitializing()}
   */
  private IPermissionCollection m_permissionCollection;
  private PermissionLevel m_level;

  public AbstractPermission(PermissionId id) {
    super(IIds.toString(id));
  }

  @Override
  public PermissionId getId() {
    return PermissionId.of(getName());
  }

  protected IPermissionCollection getPermissionCollection() {
    return m_permissionCollection;
  }

  @Override
  public final PermissionLevel getLevel() {
    return m_level;
  }

  @Override
  public final void setLevelInternal(PermissionLevel level) {
    assertInitializing();
    m_level = level;
  }

  @Override
  public String getAccessCheckFailedMessage() {
    return TEXTS.get("YouAreNotAuthorizedToPerformThisAction");
  }

  @Override
  public boolean matches(IPermission p) {
    return p != null && p.getClass() == getClass() && getId().equals(p.getId());
  }

  @Override
  public boolean implies(Permission permission) {
    if (permission instanceof IPermission) {
      return implies((IPermission) permission);
    }
    return false;
  }

  @Override
  public boolean implies(IPermission p) {
    if (matches(p) && getLevel() != PermissionLevel.NONE) {
      return evalPermission(p);
    }

    return false;
  }

  /**
   * Precondition: <code>matches(p) && getLevel() != PermissionLevel.NONE</code>
   */
  protected boolean evalPermission(IPermission p) {
    return true;
  }

  protected void assertInitializing() {
    Assertions.assertNull(m_permissionCollection, "Permission already initialized and can not be modified anymore");
  }

  @Override
  public void assignPermissionCollection(IPermissionCollection permissionCollection) {
    assertInitializing();
    initialize(permissionCollection);
    validate(permissionCollection);
    m_permissionCollection = Assertions.assertNotNull(permissionCollection);
  }

  protected void initialize(IPermissionCollection permissionCollection) {
    // method hook
  }

  protected void validate(IPermissionCollection permissionCollection) {
    validateLevel();
  }

  protected void validateLevel() {
    Assertions.assertNotNull(m_level, "Granted level is not set on {}", this);
  }

  @Override
  public String getActions() {
    return "";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + getName().hashCode();
    result = prime * result + ((m_level == null) ? 0 : m_level.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    AbstractPermission other = (AbstractPermission) obj;
    if (!getName().equals(other.getName())) {
      return false;
    }
    if (m_level == null) {
      if (other.m_level != null) {
        return false;
      }
    }
    else if (!m_level.equals(other.m_level)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " [name=" + getName() + (m_level != null ? ", level=" + m_level : "") + "]";
  }
}
