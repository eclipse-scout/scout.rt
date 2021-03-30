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

/**
 * A structure migration context data that is local to a single thread.
 * <p>
 * Only implement this interface directly instead of {@link IDoStructureMigrationTargetContextData} if
 * {@link #getIdentifierClass()} is used (because local context data cannot be pushed manually to
 * {@link DoStructureMigrationContext}, only classes implementing {@link IDoStructureMigrationTargetContextData} are
 * auto-created when a data object is traversed).
 * <p>
 * A local context data is stacked, i.e. if multiple context data for the same {@link #getIdentifierClass()} are added,
 * the last one is retrieved.
 */
public interface IDoStructureMigrationLocalContextData {

  /**
   * @return Class used as identifier for context data (map key).
   */
  default Class<? extends IDoStructureMigrationLocalContextData> getIdentifierClass() {
    return this.getClass();
  }
}
