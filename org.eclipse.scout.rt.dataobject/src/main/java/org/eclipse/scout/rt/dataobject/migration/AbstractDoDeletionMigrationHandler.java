/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
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
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;

/**
 * Abstract implementation of a {@link IDoStructureMigrationHandler} to ensure that instances of deleted {@link IDoEntity} classes have been removed from persisted data objects.
 * This migration handler will throw an exception, whenever it encounters a data object with a type name of a deleted {@link IDoEntity} class. <br>
 * When a data object with type version is deleted (i.e. a sub-class of {@link IDoEntity} with a {@link @TypeVersion} annotation) and was part of another data object, make sure to implement a {@link IDoStructureMigrationHandler} which migrates and
 * removes the respective data objects. In addition, implement a sub-class of {@link AbstractDoDeletionMigrationHandler} to ensure that all occurrences of the deleted {@link IDoEntity} class have been removed.
 */
public abstract class AbstractDoDeletionMigrationHandler implements IDoStructureMigrationHandler {

  private final NamespaceVersion m_toTypeVersion;

  protected AbstractDoDeletionMigrationHandler() {
    m_toTypeVersion = BEANS.get(toTypeVersionClass()).getVersion();
  }

  @Override
  public NamespaceVersion toTypeVersion() {
    return m_toTypeVersion;
  }

  public abstract Class<? extends ITypeVersion> toTypeVersionClass();

  @Override
  public boolean applyMigration(DataObjectMigrationContext ctx, IDoEntity doEntity) {
    throw new PlatformException("Unexpected data object with type name %s, all occurrences should have been removed by other migration handlers.", getTypeNames().toString());
  }
}
