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

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.Bean;

/**
 * Thread-safe implementation of intermediate migration container.
 */
@Bean
public class DataObjectIntermediateMigrationContextData implements IDataObjectMigrationGlobalContextData {

  protected final Queue<IDataObjectIntermediateMigration<?>> m_intermediateMigrations = new ConcurrentLinkedQueue<>();

  /**
   * Adds a new intermediate migration.
   * <p>
   * Intermediate migrations are applied in the order added here. Local intermediate migrations directly provided for
   * the call to a migrate data object method within {@link DataObjectMigrator} are applied after migrations added here.
   */
  public void add(IDataObjectIntermediateMigration<?> intermediateMigration) {
    m_intermediateMigrations.add(intermediateMigration);
  }

  /**
   * Internal usage only.
   * <p>
   * Intermediate migrations added to context are usually based on the most generic type expected during data object
   * migration (e.g. {@link IDoEntity}). The <code>valueType</code> is used to return a list of the appropriate type.
   *
   * @return All intermediate migrations.
   */
  protected <T extends IDataObject> List<IDataObjectIntermediateMigration<T>> all(Class<T> valueType) {
    //noinspection unchecked
    return m_intermediateMigrations.stream().map(intermediateMigration -> (IDataObjectIntermediateMigration<T>) intermediateMigration).collect(Collectors.toList());
  }
}
