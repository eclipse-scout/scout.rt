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

import java.util.Set;

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;

/**
 * Interface for a data object structure migration handler. Typical use cases include renaming of a data objects
 * {@link TypeName} or renaming or type change of attributes.
 * <p>
 * In contrast to {@link IDoValueMigrationHandler}, a structure migration handler operates on <i>untyped</i> objects
 * (i.e. raw {@link IDoEntity} objects) and its implementation must not access constants and attribute names from the
 * current code base (but must use string literals instead), such that a structure migration handler is not required to
 * change if the code base is changed.
 */
@ApplicationScoped
public interface IDoStructureMigrationHandler {

  /**
   * Type version triggering the migration.
   * <p>
   * A migration handler is only called when the type name matches (see {@link #getTypeNames()}), the type version
   * namespaces are equal and the type version of the data object is lower than the version of the migration handler.
   * <p>
   * This type version is usually the one the migrated data object must have after
   * {@link #applyMigration(DataObjectMigrationContext, IDoEntity)} was called. Two exceptions:
   * <ul>
   * <li>A data object changes its namespace (e.g. triggered with bravo-2 but updated to alfa-3)
   * <li>A data object is replaced by an existing one: no type version update (e.g. LoremDo is integrated into IpsumDo)
   * </ul>
   *
   * @return non-null namespace version.
   */
  NamespaceVersion toTypeVersion();

  /**
   * {@link #applyMigration(DataObjectMigrationContext, IDoEntity)} is called for these type names if type version
   * requirements are satisfied (see {@link #toTypeVersion()}).
   * <p>
   * One migration handler may be triggered by several different type names (e.g. one migration handler for a bunch of
   * renamings or data objects with a common structure).
   *
   * @return non-empty set.
   */
  Set<String> getTypeNames();

  /**
   * Migrates the given data object.
   * <p>
   * The type version update must be part of the migration, i.e. the implementation must guarantee that the type version
   * of the corresponding data object is updated.
   *
   * @param ctx
   *     Context
   * @param doEntity
   *     Raw DO entity to apply migration, according to {@link #getTypeNames()} (non-<code>null</code>)
   * @return <code>true</code> if data object was changed in any way (including type version update only),
   * <code>false</code> otherwise.
   */
  boolean applyMigration(DataObjectMigrationContext ctx, IDoEntity doEntity);
}
