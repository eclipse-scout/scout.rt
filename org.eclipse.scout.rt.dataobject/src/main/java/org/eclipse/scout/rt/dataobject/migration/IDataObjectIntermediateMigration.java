/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.migration;

import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.migration.DoStructureMigrator.DoStructureMigratorResult;

/**
 * For a {@link IDoStructureMigrationHandler} or a {@link IDoValueMigrationHandler} the migration handler is called for
 * a matching data object when visited recursively. In contrast, an intermediate migration is called once for the data
 * object provided to any of the migrate data object methods within {@link DoStructureMigrator}. It's called after
 * structure migration was applied but before value migration is applied. This enables an implementation to modify the
 * data object so that value migration can be applied correctly.
 * <p>
 * There is no support for a kind of migration that is called before the structure migration or after the value
 * migration because a caller of {@link DoStructureMigrator} is able to do that by itself.
 * <p>
 * Different intermediate migrations must not interfere with each other. The execution order is based on the order they
 * are added to {@link DataObjectIntermediateMigrationContextData#add(IDataObjectIntermediateMigration)} and in the
 * order local intermediate migrations are provided as argument to a migrate data object method within
 * {@link DoStructureMigrator}.
 */
public interface IDataObjectIntermediateMigration<T extends IDataObject> {

  /**
   * Applies an intermediate migration.
   *
   * @param ctx
   *          Migration context
   * @param typedDataObject
   *          Typed data object (before value migration but after structure migration was applied)
   * @return Result with typed data object and a flag if a migration was applied. Returned typed data object is used
   *         even if changed flag is <code>false</code>.
   */
  DoStructureMigratorResult<T> applyMigration(DoStructureMigrationContext ctx, T typedDataObject);
}
