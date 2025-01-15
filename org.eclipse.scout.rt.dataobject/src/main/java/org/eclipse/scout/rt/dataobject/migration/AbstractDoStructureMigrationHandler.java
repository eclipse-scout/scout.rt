/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.migration;

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.ITypeVersion;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;

/**
 * Abstract implementation of a {@link IDoStructureMigrationHandler} supporting {@link Class} of {@link ITypeVersion}
 * instead of {@link NamespaceVersion} and auto-update of type version to {@link #toTypeVersion()}.
 */
public abstract class AbstractDoStructureMigrationHandler implements IDoStructureMigrationHandler {

  private final NamespaceVersion m_toTypeVersion;

  protected AbstractDoStructureMigrationHandler() {
    m_toTypeVersion = BEANS.get(toTypeVersionClass()).getVersion();
  }

  @Override
  public NamespaceVersion toTypeVersion() {
    return m_toTypeVersion;
  }

  public abstract Class<? extends ITypeVersion> toTypeVersionClass();

  @Override
  public boolean applyMigration(DataObjectMigrationContext ctx, IDoEntity doEntity) {
    boolean changed = false;
    changed |= migrate(ctx, doEntity);
    changed |= updateTypeVersion(doEntity); // new type version needs to be applied because migration is only completed when data object has an updated type version
    return changed;
  }

  /**
   * For convenience. Same as {@link #applyMigration(DataObjectMigrationContext, IDoEntity)} except that the type
   * version of the data object itself must not be updated, the caller of this method takes care of handling type
   * version updates.
   * <p>
   * If special type version update handling is required, override {@link #updateTypeVersion(IDoEntity)}
   *
   * @param ctx
   *     Context
   * @param doEntity
   *     Do entity to apply migration, according to {@link #getTypeNames()} (non-<code>null</code>)
   * @return <code>true</code> if data object was changed in any way, <code>false</code> otherwise.
   */
  protected abstract boolean migrate(DataObjectMigrationContext ctx, IDoEntity doEntity);

  /**
   * Updates the type version of the data object to {@link #toTypeVersion()}.
   *
   * @return <code>true</code> if the new type version was set, <code>false</code> if the type version was already
   * up-to-date (equal).
   */
  protected boolean updateTypeVersion(IDoEntity doEntity) {
    return BEANS.get(DoStructureMigrationHelper.class).updateTypeVersion(doEntity, toTypeVersion());
  }
}
