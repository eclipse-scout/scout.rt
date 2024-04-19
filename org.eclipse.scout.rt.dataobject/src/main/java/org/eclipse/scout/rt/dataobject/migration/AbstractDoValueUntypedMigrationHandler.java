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

import org.eclipse.scout.rt.dataobject.ITypeVersion;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

/**
 * Abstract implementation of a {@link IDoValueMigrationHandler} providing an implementation of {@link #valueClass()}
 * based on the class generic type and supporting {@link Class} of {@link ITypeVersion} instead of
 * {@link NamespaceVersion}.
 * <p>
 * This untyped migration handler allows to rename the type name and therefore change the type of the migrated value
 * from {@code T} to {@link Object}.
 */
public abstract class AbstractDoValueUntypedMigrationHandler<T> implements IDoValueMigrationHandler<T> {

  private final NamespaceVersion m_typeVersion;

  protected AbstractDoValueUntypedMigrationHandler() {
    m_typeVersion = BEANS.get(typeVersionClass()).getVersion();
  }

  @Override
  public double primarySortOrder() {
    return UNTYPED_PRIMARY_SORT_ORDER;
  }

  @Override
  public NamespaceVersion typeVersion() {
    return m_typeVersion;
  }

  public abstract Class<? extends ITypeVersion> typeVersionClass();

  @Override
  public Class<T> valueClass() {
    // noinspection unchecked
    return TypeCastUtility.getGenericsParameterClass(this.getClass(), AbstractDoValueUntypedMigrationHandler.class);
  }

  /**
   * The default implementation will accept when this value migration wasn't applied already. Own implementations might
   * choose to always accept (despite being already applied) or to not accept in case some context data is missing.
   */
  @Override
  public boolean accept(DataObjectMigrationContext ctx) {
    return !ctx.getGlobal(DoValueMigrationIdsContextData.class).getAppliedValueMigrationIds().contains(id());
  }
}
