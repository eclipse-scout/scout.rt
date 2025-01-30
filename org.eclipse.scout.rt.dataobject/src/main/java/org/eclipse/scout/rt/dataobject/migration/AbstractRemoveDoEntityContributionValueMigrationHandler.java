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

import org.eclipse.scout.rt.dataobject.DataObjectHelper;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.IDoEntityContribution;
import org.eclipse.scout.rt.dataobject.ITypeVersion;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;
import org.eclipse.scout.rt.platform.BEANS;

/**
 * Abstract implementation of a {@link IDoValueMigrationHandler} to remove an {@link IDoEntityContribution} that does not exist anymore in the code.
 * <p>
 * In case the {@link IDoEntityContribution} still exists in the code this abstract implementation will not do anything, because in this case the migration handler does not know the original {@link TypeVersion} of the contribution.<br>
 * If you still want to remove the contribution - even if it still exists in the code - you need to create your own migration handler.
 *
 * @param <T>
 *     The {@link IDoEntity} that contains the contribution to be removed.
 */
public abstract class AbstractRemoveDoEntityContributionValueMigrationHandler<T extends IDoEntity> extends AbstractDoValueMigrationHandler<T> {

  /**
   * @return value of {@link TypeName} of the contribution to be removed, e.g. "scout.MyContribution"
   */
  protected abstract String getContributionTypeName();

  /**
   * @return value of {@link TypeVersion} of the contribution to be removed
   */
  protected abstract Class<? extends ITypeVersion> getContributionTypeVersionClass();

  protected String getContributionTypeVersion() {
    return BEANS.get(getContributionTypeVersionClass()).getVersion().unwrap();
  }

  @Override
  public double primarySortOrder() {
    return REMOVE_CONTRIBUTION_PRIMARY_SORT_ORDER;
  }

  @Override
  public T migrate(DataObjectMigrationContext ctx, T value) {
    T clone = BEANS.get(DataObjectHelper.class).clone(value);
    //noinspection deprecation
    clone.getAllContributions().removeIf(this::matchesContribution);
    return clone;
  }

  protected boolean matchesContribution(IDoEntity contribution) {
    if (contribution == null) {
      return false;
    }
    return getContributionTypeName().equals(contribution.get(DoStructureMigrationHelper.TYPE_ATTRIBUTE_NAME, String.class))
        && getContributionTypeVersion().equals(contribution.get(DoStructureMigrationHelper.TYPE_VERSION_ATTRIBUTE_NAME, String.class));
  }
}
