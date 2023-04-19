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

import java.util.Collection;
import java.util.Collections;

import org.eclipse.scout.rt.dataobject.AbstractTypeVersion;
import org.eclipse.scout.rt.dataobject.AbstractTypeVersionTest;
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

  /**
   * Only used for {@link AbstractTypeVersionTest#testFromClassName()}.
   */
  @IgnoreBean
  public static final class DataObjectFixture_1_3_0__suffix extends AbstractTypeVersion {
  }

  /**
   * Only used for {@link AbstractTypeVersionTest#testFromClassName()}.
   */
  @IgnoreBean
  public static final class DataObjectFixture_1_3_0__123456 extends AbstractTypeVersion {
  }

  /**
   * Only used for {@link AbstractTypeVersionTest#testFromClassName()}.
   */
  @IgnoreBean
  public static final class DataObjectFixture_1_3_0__lorem_ipsum extends AbstractTypeVersion {
  }

  /**
   * Only used for {@link AbstractTypeVersionTest#testFromClassName()}.
   */
  @IgnoreBean
  public static final class DataObjectFixture_1_3_0__loremIpsumDolor_3 extends AbstractTypeVersion {
  }

  /**
   * Only used for {@link AbstractTypeVersionTest#testFromClassName()}.
   */
  @IgnoreBean
  public static final class DataObjectFixture_1_3_002__suffix extends AbstractTypeVersion {
  }
}
