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
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;

/**
 * Abstract implementation of a {@link IDoValueMigrationHandler} providing an implementation of {@link #valueClass()}
 * based on the class generic type and supporting {@link Class} of {@link ITypeVersion} instead of
 * {@link NamespaceVersion}.
 *
 * @see AbstractDoValueUntypedMigrationHandler for a handler allowing to rename and therefore change the type T of the
 *      migrated value.
 */
public abstract class AbstractDoValueMigrationHandler<T> extends AbstractDoValueUntypedMigrationHandler<T> {

  /**
   * Note: A default data object value migration is not allowed to change the type {@code T} of the value. Use
   * {@link AbstractDoValueUntypedMigrationHandler} to change the type of the migrated value.
   */
  @Override
  public abstract T migrate(DataObjectMigrationContext ctx, T value);
}
