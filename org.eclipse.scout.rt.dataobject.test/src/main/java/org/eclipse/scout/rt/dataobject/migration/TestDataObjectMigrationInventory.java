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

import static org.eclipse.scout.rt.platform.util.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.dataobject.ITypeVersion;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.namespace.INamespace;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

@IgnoreBean
public class TestDataObjectMigrationInventory extends DataObjectMigrationInventory {

  protected final List<INamespace> m_internalNamespaces = new ArrayList<>();
  protected final Collection<ITypeVersion> m_internalTypeVersions = new ArrayList<>();
  protected final Collection<Class<? extends IDoStructureMigrationTargetContextData>> m_internalContextDataClasses = new ArrayList<>();
  protected final List<IDoStructureMigrationHandler> m_internalStructureMigrationHandlers = new ArrayList<>();
  protected final List<IDoValueMigrationHandler<?>> m_internalValueMigrationHandlers = new ArrayList<>();

  public TestDataObjectMigrationInventory(
      List<INamespace> namespaces,
      Collection<ITypeVersion> typeVersions,
      Collection<Class<? extends IDoStructureMigrationTargetContextData>> contextDataClasses,
      Collection<IDoStructureMigrationHandler> structureMigrationHandlers,
      Collection<IDoValueMigrationHandler<?>> valueMigrationHandlers) {
    assertFalse(CollectionUtility.isEmpty(namespaces), "namespaces must be set");
    assertFalse(CollectionUtility.isEmpty(typeVersions), "typeVersions must be set");

    m_internalNamespaces.addAll(namespaces);
    m_internalTypeVersions.addAll(typeVersions);
    if (contextDataClasses != null) {
      m_internalContextDataClasses.addAll(contextDataClasses);
    }

    if (structureMigrationHandlers != null) {
      m_internalStructureMigrationHandlers.addAll(structureMigrationHandlers);
    }
    if (valueMigrationHandlers != null) {
      m_internalValueMigrationHandlers.addAll(valueMigrationHandlers);
    }

    init();
  }

  @Override
  protected List<INamespace> getAllNamespaces() {
    return m_internalNamespaces;
  }

  @Override
  protected Collection<ITypeVersion> getAllTypeVersions() {
    return m_internalTypeVersions;
  }

  @Override
  protected Collection<Class<? extends IDoStructureMigrationTargetContextData>> getAllContextDataClasses() {
    return m_internalContextDataClasses;
  }

  @Override
  protected List<IDoStructureMigrationHandler> getAllStructureMigrationHandlers() {
    return m_internalStructureMigrationHandlers;
  }

  @Override
  protected List<IDoValueMigrationHandler<?>> getAllValueMigrationHandlers() {
    return m_internalValueMigrationHandlers;
  }
}
