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

import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;

@IgnoreBean
public class TestDataObjectMigrator extends DataObjectMigrator {

  /**
   * Override to change visibility from protected to public.
   */
  @Override
  public boolean applyStructureMigration(DataObjectMigrationContext ctx, IDataObject dataObject, NamespaceVersion toVersion) {
    return super.applyStructureMigration(ctx, dataObject, toVersion);
  }
}
