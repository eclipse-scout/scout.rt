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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.dataobject.AbstractDataObjectVisitor;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;

/**
 * Data object visitor to migrate data objects.
 */
public class MigrationDataObjectVisitor extends AbstractDataObjectVisitor {

  protected final DoStructureMigrationHelper m_helper;
  protected final DoStructureMigrationInventory m_inventory;

  protected final DoStructureMigrationContext m_ctx;
  protected final NamespaceVersion m_version;
  protected final Map<String, IDoStructureMigrationHandler> m_migrationHandlerPerTypeName;

  protected boolean m_changed = false;

  public MigrationDataObjectVisitor(DoStructureMigrationContext ctx, NamespaceVersion version) {
    m_helper = BEANS.get(DoStructureMigrationHelper.class);
    m_inventory = BEANS.get(DoStructureMigrationInventory.class);

    m_ctx = ctx;
    m_version = version;

    m_migrationHandlerPerTypeName = m_inventory.getMigrationHandlers(version);
  }

  public boolean isChanged() {
    return m_changed;
  }

  public void migrate(IDataObject dataObject) {
    visit(dataObject);
  }

  @Override
  protected void caseDoEntity(IDoEntity entity) {
    // Migrate data object before creating context data, otherwise, in case of recursive data objects,
    // the context data available in IDoStructureMigrationHandler#migrate would be the one from the current data object thus preventing access to parent context.
    migrateInternal(entity);

    // Create context data based on migrated data object
    List<IDoStructureMigrationTargetContextData> localContextDataList = pushLocalContextData(entity);
    try {
      super.caseDoEntity(entity);
    }
    finally {
      localContextDataList.forEach(m_ctx::remove);
    }
  }

  protected List<IDoStructureMigrationTargetContextData> pushLocalContextData(IDoEntity doEntity) {
    List<IDoStructureMigrationTargetContextData> localContextDataList = new ArrayList<>();
    Set<Class<? extends IDoStructureMigrationTargetContextData>> contextDataClasses = m_inventory.getDoMigrationContextValues(doEntity);
    for (Class<? extends IDoStructureMigrationTargetContextData> contextDataClass : contextDataClasses) {
      IDoStructureMigrationTargetContextData contextValue = BEANS.get(contextDataClass);
      if (contextValue.initialize(m_ctx, doEntity)) {
        localContextDataList.add(contextValue);
      }
    }

    localContextDataList.forEach(m_ctx::push);
    return localContextDataList;
  }

  /**
   * Migrates the data object and updates all type versions accordingly.
   */
  protected void migrateInternal(IDoEntity doEntity) {
    if (!m_helper.isMigrationApplicable(doEntity, m_version)) {
      return;
    }

    String typeName = m_helper.getType(doEntity);
    IDoStructureMigrationHandler migrationHandler = m_migrationHandlerPerTypeName.get(typeName);
    if (migrationHandler == null) {
      return;
    }

    m_changed |= migrationHandler.applyMigration(m_ctx, doEntity);
  }
}
