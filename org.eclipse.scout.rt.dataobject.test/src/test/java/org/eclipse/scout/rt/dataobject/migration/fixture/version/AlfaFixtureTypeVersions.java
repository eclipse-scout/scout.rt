/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.migration.fixture.version;

import org.eclipse.scout.rt.dataobject.AbstractTypeVersion;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;

public final class AlfaFixtureTypeVersions {

  public static final class Alfafixture_1 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(AlfaFixtureNamespace.ID, "1");

    public Alfafixture_1() {
      super(VERSION);
    }
  }

  public static final class Alfafixture_2 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(AlfaFixtureNamespace.ID, "2");

    public Alfafixture_2() {
      super(VERSION);
    }
  }

  public static final class Alfafixture_3 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(AlfaFixtureNamespace.ID, "3");

    public Alfafixture_3() {
      super(VERSION);
    }
  }

  public static final class Alfafixture_6 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(AlfaFixtureNamespace.ID, "6");

    public Alfafixture_6() {
      super(VERSION);
    }
  }

  public static final class Alfafixture_7 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(AlfaFixtureNamespace.ID, "7");

    public Alfafixture_7() {
      super(VERSION);
    }
  }
}
