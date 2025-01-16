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

import java.util.Collections;
import java.util.Map;

/**
 * Simple {@link AbstractDoValueMigrationHandler} migrating values from old to new using a {@link Map} storing all
 * possible old to new mappings. The {@link #initMigrationMap()} method is called only once during object
 * initialization.
 */
public abstract class AbstractDoValueMigrationHandlerByMap<T> extends AbstractDoValueMigrationHandler<T> {

  private final Map<T, T> m_migrationMap = Collections.unmodifiableMap(initMigrationMap());

  protected abstract Map<T, T> initMigrationMap();

  protected Map<T, T> getMigrationMap() {
    return m_migrationMap;
  }

  @Override
  public T migrate(DataObjectMigrationContext ctx, T value) {
    T migratedValue = getMigrationMap().get(value);
    return migratedValue != null ? migratedValue : value;
  }
}
