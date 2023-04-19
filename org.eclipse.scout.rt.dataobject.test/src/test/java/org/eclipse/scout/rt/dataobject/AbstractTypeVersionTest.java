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
package org.eclipse.scout.rt.dataobject;

import static org.eclipse.scout.rt.dataobject.AbstractTypeVersion.fromClassName;
import static org.eclipse.scout.rt.platform.util.Assertions.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.rt.dataobject.fixture.DataObjectFixtureTypeVersions.DataObjectFixture_1_0_0;
import org.eclipse.scout.rt.dataobject.fixture.DataObjectFixtureTypeVersions.DataObjectFixture_1_3_002__suffix;
import org.eclipse.scout.rt.dataobject.fixture.DataObjectFixtureTypeVersions.DataObjectFixture_1_3_0__123456;
import org.eclipse.scout.rt.dataobject.fixture.DataObjectFixtureTypeVersions.DataObjectFixture_1_3_0__loremIpsumDolor_3;
import org.eclipse.scout.rt.dataobject.fixture.DataObjectFixtureTypeVersions.DataObjectFixture_1_3_0__lorem_ipsum;
import org.eclipse.scout.rt.dataobject.fixture.DataObjectFixtureTypeVersions.DataObjectFixture_1_3_0__suffix;
import org.eclipse.scout.rt.dataobject.fixture.DataObjectProjectFixtureTypeVersions.DataObjectProjectFixture_1_2_3_004;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for {@link AbstractTypeVersion}
 */
public class AbstractTypeVersionTest {

  @Test
  public void testFromClassName() {
    assertNull(fromClassName(null));
    Assert.assertThrows(IllegalArgumentException.class, () -> fromClassName(AbstractTypeVersion.class));

    assertEquals(NamespaceVersion.of("dataObjectFixture", "1.0.0"), fromClassName(DataObjectFixture_1_0_0.class));
    assertEquals(NamespaceVersion.of("dataObjectProjectFixture", "1.2.3.004"), fromClassName(DataObjectProjectFixture_1_2_3_004.class));

    // with suffix
    assertEquals(NamespaceVersion.of("dataObjectFixture", "1.3.0"), fromClassName(DataObjectFixture_1_3_0__suffix.class));
    assertEquals(NamespaceVersion.of("dataObjectFixture", "1.3.0"), fromClassName(DataObjectFixture_1_3_0__123456.class));
    assertEquals(NamespaceVersion.of("dataObjectFixture", "1.3.0"), fromClassName(DataObjectFixture_1_3_0__lorem_ipsum.class));
    assertEquals(NamespaceVersion.of("dataObjectFixture", "1.3.0"), fromClassName(DataObjectFixture_1_3_0__loremIpsumDolor_3.class));
    assertEquals(NamespaceVersion.of("dataObjectFixture", "1.3.002"), fromClassName(DataObjectFixture_1_3_002__suffix.class));
  }
}
