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
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.AlfaFixture_1;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.AlfaFixture_2;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.AlfaFixtureTypeVersions.AlfaFixture_3;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;

public final class BravoFixtureTypeVersions {

  public static final class BravoFixture_1 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(BravoFixtureNamespace.ID, "1");

    public BravoFixture_1() {
      super(VERSION);
    }

    @Override
    protected Collection<Class<? extends ITypeVersion>> getDependencyClasses() {
      return Arrays.asList(AlfaFixture_1.class);
    }
  }

  public static final class BravoFixture_2 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(BravoFixtureNamespace.ID, "2");

    public BravoFixture_2() {
      super(VERSION);
    }

    @Override
    protected Collection<Class<? extends ITypeVersion>> getDependencyClasses() {
      return Arrays.asList(AlfaFixture_2.class);
    }
  }

  public static final class BravoFixture_3 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(BravoFixtureNamespace.ID, "3");

    public BravoFixture_3() {
      super(VERSION);
    }

    @Override
    protected Collection<Class<? extends ITypeVersion>> getDependencyClasses() {
      return Arrays.asList(AlfaFixture_3.class);
    }
  }
}
