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

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.dataobject.AbstractReplacingDataObjectVisitor;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

/**
 * Data object visitor to migrate data objects.
 */
public class DoValueMigrationDataObjectVisitor extends AbstractReplacingDataObjectVisitor {

  protected final DataObjectMigrationInventory m_inventory;
  protected final DataObjectMigrationContext m_ctx;

  // ordered list of value migration handlers, which have not been applied
  protected final List<IDoValueMigrationHandler<?>> m_valueMigrationHandlers;

  protected boolean m_changed = false;

  public DoValueMigrationDataObjectVisitor(DataObjectMigrationContext ctx) {
    m_inventory = BEANS.get(DataObjectMigrationInventory.class);
    m_ctx = ctx;

    Set<DoValueMigrationId> appliedValueMigrationIds = m_ctx.getGlobal(DoValueMigrationIdsContextData.class).getAppliedValueMigrationIds();
    assertNotNull(appliedValueMigrationIds, "Applied value migration IDs on context required.");
    m_valueMigrationHandlers = m_inventory.getValueMigrationHandlers().stream()
        .filter(handler -> handler.accept(ctx))
        .collect(Collectors.toList());
  }

  public boolean isChanged() {
    return m_changed;
  }

  public <T extends IDataObject> T migrate(T dataObject) {
    if (m_valueMigrationHandlers.isEmpty()) {
      return dataObject;
    }
    //noinspection unchecked
    return (T) replaceOrVisit(dataObject);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Object replaceOrVisit(Object oldValue) {
    if (oldValue == null) {
      return null;
    }
    // apply all migration handlers in order
    // feed the migrated value to the next handler
    Object currentValue = oldValue;
    for (IDoValueMigrationHandler<?> handler : m_valueMigrationHandlers) {
      if (handler.valueClass().isInstance(currentValue)) {
        // noinspection unchecked
        Object migratedValue = ((IDoValueMigrationHandler<Object>) handler).migrate(m_ctx, currentValue);
        // handler must not change provided input value, but must return a fresh instance (clone or new instance)
        m_changed |= ObjectUtility.notEquals(currentValue, migratedValue);
        currentValue = migratedValue;
      }
    }
    // recursively visit migrated value
    return super.replaceOrVisit(currentValue);
  }
}
