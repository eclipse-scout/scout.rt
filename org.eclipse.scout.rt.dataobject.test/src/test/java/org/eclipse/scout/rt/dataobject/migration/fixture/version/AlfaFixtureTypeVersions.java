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
package org.eclipse.scout.rt.dataobject.migration.fixture.version;

import org.eclipse.scout.rt.dataobject.AbstractTypeVersion;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;

public final class AlfaFixtureTypeVersions {

  public static final class AlfaFixture_1 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(AlfaFixtureNamespace.ID, "1");

    public AlfaFixture_1() {
      super(VERSION);
    }
  }

  public static final class AlfaFixture_2 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(AlfaFixtureNamespace.ID, "2");

    public AlfaFixture_2() {
      super(VERSION);
    }
  }

  public static final class AlfaFixture_3 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(AlfaFixtureNamespace.ID, "3");

    public AlfaFixture_3() {
      super(VERSION);
    }
  }

  public static final class AlfaFixture_6 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(AlfaFixtureNamespace.ID, "6");

    public AlfaFixture_6() {
      super(VERSION);
    }
  }

  public static final class AlfaFixture_7 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(AlfaFixtureNamespace.ID, "7");

    public AlfaFixture_7() {
      super(VERSION);
    }
  }
}
