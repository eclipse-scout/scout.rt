/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.migration;

import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;

@IgnoreBean
// TODO 23.1 [data object migration] rename to TestDataObjectMigrator
public class TestDoStructureMigrator extends DoStructureMigrator {

  /**
   * Override to change visibility from protected to public.
   */
  @Override
  public boolean applyStructureMigration(DoStructureMigrationContext ctx, IDataObject dataObject, NamespaceVersion toVersion) {
    return super.applyStructureMigration(ctx, dataObject, toVersion);
  }
}
