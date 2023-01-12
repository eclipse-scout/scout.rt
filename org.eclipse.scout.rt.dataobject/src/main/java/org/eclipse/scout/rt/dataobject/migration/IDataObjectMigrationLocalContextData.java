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

/**
 * A structure migration context data that is local to a single thread.
 * <p>
 * Only implement this interface directly instead of {@link IDoStructureMigrationTargetContextData} if
 * {@link #getIdentifierClass()} is used (because local context data cannot be pushed manually to
 * {@link DataObjectMigrationContext}, only classes implementing {@link IDoStructureMigrationTargetContextData} are
 * auto-created when a data object is traversed).
 * <p>
 * A local context data is stacked, i.e. if multiple context data for the same {@link #getIdentifierClass()} are added,
 * the last one is retrieved.
 */
public interface IDataObjectMigrationLocalContextData {

  /**
   * @return Class used as identifier for context data (map key).
   */
  default Class<? extends IDataObjectMigrationLocalContextData> getIdentifierClass() {
    return this.getClass();
  }
}
