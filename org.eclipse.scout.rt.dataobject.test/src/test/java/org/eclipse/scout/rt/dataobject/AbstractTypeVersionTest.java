/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject;

import static org.eclipse.scout.rt.dataobject.AbstractTypeVersion.fromClassName;
import static org.eclipse.scout.rt.platform.util.Assertions.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.rt.dataobject.fixture.DataObjectFixtureTypeVersions.DataObjectFixture_1_0_0;
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
  }
}
