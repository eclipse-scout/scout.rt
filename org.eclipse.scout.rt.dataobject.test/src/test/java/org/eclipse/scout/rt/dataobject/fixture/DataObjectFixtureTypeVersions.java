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

import java.util.Collection;
import java.util.Collections;

import org.eclipse.scout.rt.dataobject.AbstractTypeVersion;
import org.eclipse.scout.rt.dataobject.DataObjectInventoryTest;
import org.eclipse.scout.rt.dataobject.ITypeVersion;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;

public final class DataObjectFixtureTypeVersions {

  public static final class DataObjectFixture_1_0_0 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(DataObjectFixtureNamespace.ID, "1.0.0");

    public DataObjectFixture_1_0_0() {
      super(VERSION);
    }
  }

  public static final class DataObjectFixture_1_0_0_034 extends AbstractTypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of(DataObjectFixtureNamespace.ID, "1.0.0.034");

    public DataObjectFixture_1_0_0_034() {
      super(VERSION);
    }
  }

  /**
   * Manually registered in {@link DataObjectInventoryTest}.
   */
  @IgnoreBean
  public static final class DataObjectFixture_No_Version implements ITypeVersion {

    @Override
    public NamespaceVersion getVersion() {
      return null;
    }

    @Override
    public Collection<NamespaceVersion> getDependencies() {
      return Collections.emptyList();
    }
  }

  /**
   * Manually registered in {@link DataObjectInventoryTest}.
   */
  @IgnoreBean
  public static final class NonRegisteredNamespaceFixture_1_0_0 implements ITypeVersion {

    public static final NamespaceVersion VERSION = NamespaceVersion.of("noRegisteredNamespace", "1.0.0");

    @Override
    public NamespaceVersion getVersion() {
      return VERSION;
    }

    @Override
    public Collection<NamespaceVersion> getDependencies() {
      return Collections.emptyList();
    }
  }

  /**
   * Manually registered in {@link DataObjectInventoryTest}.
   */
  @IgnoreBean
  public static final class DataObjectFixture_1_0_0_Duplicate implements ITypeVersion {

    @Override
    public NamespaceVersion getVersion() {
      return DataObjectFixture_1_0_0.VERSION;
    }

    @Override
    public Collection<NamespaceVersion> getDependencies() {
      return Collections.emptyList();
    }
  }
}
