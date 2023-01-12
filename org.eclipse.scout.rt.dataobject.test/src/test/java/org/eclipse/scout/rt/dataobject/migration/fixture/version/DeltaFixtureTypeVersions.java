/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
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
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_1;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_2;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;

public final class DeltaFixtureTypeVersions {

  public static final class DeltaFixture_1 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(DeltaFixtureNamespace.ID, "1");

    public DeltaFixture_1() {
      super(VERSION);
    }

    @Override
    protected Collection<Class<? extends ITypeVersion>> getDependencyClasses() {
      return Arrays.asList(CharlieFixture_1.class);
    }
  }

  public static final class DeltaFixture_2 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(DeltaFixtureNamespace.ID, "2");

    public DeltaFixture_2() {
      super(VERSION);
    }

    @Override
    protected Collection<Class<? extends ITypeVersion>> getDependencyClasses() {
      return Arrays.asList(CharlieFixture_2.class);
    }
  }
}
