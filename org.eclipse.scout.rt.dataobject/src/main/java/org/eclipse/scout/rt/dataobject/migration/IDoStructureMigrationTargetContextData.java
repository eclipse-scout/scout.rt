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
import org.eclipse.scout.rt.platform.Bean;

/**
 * Interface for migration context data classes that are created and pushed as local context data to
 * {@link DataObjectMigrationContext} when a corresponding data object is traversed (see
 * {@link DoStructureMigrationContextDataTarget} annotation for data object matching).
 */
@Bean
public interface IDoStructureMigrationTargetContextData extends IDataObjectMigrationLocalContextData {

  /**
   * A context for a data object is initialized after the corresponding data object was migrated to the specific
   * version.
   *
   * @return <code>true</true> if it's a valid context and should be used, <code>false</false> to discard context.
   **/
  boolean initialize(DataObjectMigrationContext ctx, IDoEntity doEntity);
}
