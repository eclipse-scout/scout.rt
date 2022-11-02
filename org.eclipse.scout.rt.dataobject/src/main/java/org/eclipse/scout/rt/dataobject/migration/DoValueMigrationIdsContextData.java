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

import java.util.Collections;
import java.util.Set;

import org.eclipse.scout.rt.platform.Bean;

/**
 * Context to provide a set of all value migration IDs, which have been applied on the current system already, they will
 * not be applied again during data object migration.
 */
@Bean
public class DoValueMigrationIdsContextData implements IDoStructureMigrationGlobalContextData {

  // Thead-safe because read-only after initialization.
  // An empty set implies that all available value migrations will be applied. In case of a null value, value migrations will be skipped.
  protected Set<DoValueMigrationId> m_appliedValueMigrationIds;

  public DoValueMigrationIdsContextData withAppliedValueMigrationIds(Set<DoValueMigrationId> appliedValueMigrationIds) {
    m_appliedValueMigrationIds = appliedValueMigrationIds == null ? null : Collections.unmodifiableSet(appliedValueMigrationIds);
    return this;
  }

  /**
   * @return unmodifiable set of value migration IDs
   */
  public Set<DoValueMigrationId> getAppliedValueMigrationIds() {
    return m_appliedValueMigrationIds;
  }
}
