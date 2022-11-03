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

import static org.eclipse.scout.rt.platform.util.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.dataobject.ITypeVersion;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.namespace.INamespace;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

@IgnoreBean
// TODO 23.1 [data object migration] rename to TestDataObjectMigrationInventory
public class TestDoStructureMigrationInventory extends DoStructureMigrationInventory {

  protected final List<INamespace> m_internalNamespaces = new ArrayList<>();
  protected final Collection<ITypeVersion> m_internalTypeVersions = new ArrayList<>();
  protected final Collection<Class<? extends IDoStructureMigrationTargetContextData>> m_internalContextDataClasses = new ArrayList<>();
  protected final List<IDoStructureMigrationHandler> m_internalStructureMigrationHandlers = new ArrayList<>();
  protected final List<IDoValueMigrationHandler<?>> m_internalValueMigrationHandlers = new ArrayList<>();

  public TestDoStructureMigrationInventory(
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

  /**
   * This constructor will be removed in a future release.
   *
   * @deprecated use {@link #TestDoStructureMigrationInventory(List, Collection, Collection, Collection, Collection)}
   *             instead.
   */
  // TODO 23.1 [data object migration] remove deprecated constructor
  @Deprecated
  public TestDoStructureMigrationInventory(
      List<INamespace> namespaces,
      Collection<ITypeVersion> typeVersions,
      Collection<Class<? extends IDoStructureMigrationTargetContextData>> contextDataClasses,
      IDoStructureMigrationHandler... structureMigrationHandlers) {
    this(namespaces, typeVersions, contextDataClasses, CollectionUtility.arrayList(structureMigrationHandlers), Collections.emptyList());
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
