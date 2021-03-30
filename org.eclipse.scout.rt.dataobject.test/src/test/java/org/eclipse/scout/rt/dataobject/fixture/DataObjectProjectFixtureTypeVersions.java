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
