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

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.scout.rt.dataobject.AbstractTypeVersion;
import org.eclipse.scout.rt.dataobject.ITypeVersion;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.BravoFixtureTypeVersions.Bravofixture_1;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.BravoFixtureTypeVersions.Bravofixture_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.BravoFixtureTypeVersions.Bravofixture_3;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;

public final class CharlieFixtureTypeVersions {

  public static final class Charliefixture_1 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(CharlieFixtureNamespace.ID, "1");

    public Charliefixture_1() {
      super(VERSION);
    }

    @Override
    protected Collection<Class<? extends ITypeVersion>> getDependencyClasses() {
      return Arrays.asList(Bravofixture_1.class);
    }
  }

  public static final class Charliefixture_2 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(CharlieFixtureNamespace.ID, "2");

    public Charliefixture_2() {
      super(VERSION);
    }

    @Override
    protected Collection<Class<? extends ITypeVersion>> getDependencyClasses() {
      return Arrays.asList(Bravofixture_2.class);
    }
  }

  public static final class Charliefixture_3 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(CharlieFixtureNamespace.ID, "3");

    public Charliefixture_3() {
      super(VERSION);
    }

    @Override
    protected Collection<Class<? extends ITypeVersion>> getDependencyClasses() {
      return Arrays.asList(Bravofixture_3.class);
    }
  }

  public static final class Charliefixture_4 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(CharlieFixtureNamespace.ID, "4");

    public Charliefixture_4() {
      super(VERSION);
    }
  }

  public static final class Charliefixture_5 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(CharlieFixtureNamespace.ID, "5");

    public Charliefixture_5() {
      super(VERSION);
    }
  }
}
