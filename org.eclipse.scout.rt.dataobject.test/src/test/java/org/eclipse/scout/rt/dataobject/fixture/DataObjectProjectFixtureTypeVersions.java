/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.fixture;

import org.eclipse.scout.rt.dataobject.AbstractTypeVersion;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;

public final class DataObjectProjectFixtureTypeVersions {

  private DataObjectProjectFixtureTypeVersions() {
  }

  public static final class DataObjectProjectFixture_1_2_3_004 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(DataObjectProjectFixtureNamespace.ID, "1.2.3.004");

    public DataObjectProjectFixture_1_2_3_004() {
      super(VERSION);
    }
  }
}
