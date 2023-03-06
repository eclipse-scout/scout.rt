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
 * Handles the deletion of <i>non-root</i> {@link IDoEntity DO entitites}. Any other DOs referencing the deleted one
 * need to migrate accordingly and remove it to ensure that an actual implementation of this migration is never called.
 * Implementations do <i>not</i> need a unit test and are ignored by any migration handler completeness tests.
 * <p>
 * Abstract implementation of a {@link IDoStructureMigrationHandler} supporting {@link Class} of {@link ITypeVersion}
 * instead of {@link NamespaceVersion} and auto-update of type version to {@link #toTypeVersion()}.
 */
public abstract class AbstractDoDeletionMigrationHandler implements IDoStructureMigrationHandler {

  private final NamespaceVersion m_toTypeVersion;

  public AbstractDoDeletionMigrationHandler() {
    m_toTypeVersion = BEANS.get(toTypeVersionClass()).getVersion();
  }

  @Override
  public NamespaceVersion toTypeVersion() {
    return m_toTypeVersion;
  }

  public abstract Class<? extends ITypeVersion> toTypeVersionClass();

  @Override
  public boolean applyMigration(DataObjectMigrationContext ctx, IDoEntity doEntity) {
    throw new UnsupportedOperationException(String.format("Unexpected DOs of types %s, all occurrences should have been deleted.", getTypeNames().toString()));
  }
}
