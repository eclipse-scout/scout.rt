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

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.scout.rt.dataobject.AbstractTypeVersion;
import org.eclipse.scout.rt.dataobject.ITypeVersion;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.BravoFixtureTypeVersions.BravoFixture_1;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.BravoFixtureTypeVersions.BravoFixture_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.BravoFixtureTypeVersions.BravoFixture_3;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;

public final class CharlieFixtureTypeVersions {

  public static final class CharlieFixture_1 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(CharlieFixtureNamespace.ID, "1");

    public CharlieFixture_1() {
      super(VERSION);
    }

    @Override
    protected Collection<Class<? extends ITypeVersion>> getDependencyClasses() {
      return Arrays.asList(BravoFixture_1.class);
    }
  }

  public static final class CharlieFixture_2 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(CharlieFixtureNamespace.ID, "2");

    public CharlieFixture_2() {
      super(VERSION);
    }

    @Override
    protected Collection<Class<? extends ITypeVersion>> getDependencyClasses() {
      return Arrays.asList(BravoFixture_2.class);
    }
  }

  public static final class CharlieFixture_3 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(CharlieFixtureNamespace.ID, "3");

    public CharlieFixture_3() {
      super(VERSION);
    }

    @Override
    protected Collection<Class<? extends ITypeVersion>> getDependencyClasses() {
      return Arrays.asList(BravoFixture_3.class);
    }
  }

  public static final class CharlieFixture_4 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(CharlieFixtureNamespace.ID, "4");

    public CharlieFixture_4() {
      super(VERSION);
    }
  }

  public static final class CharlieFixture_5 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(CharlieFixtureNamespace.ID, "5");

    public CharlieFixture_5() {
      super(VERSION);
    }
  }
}
