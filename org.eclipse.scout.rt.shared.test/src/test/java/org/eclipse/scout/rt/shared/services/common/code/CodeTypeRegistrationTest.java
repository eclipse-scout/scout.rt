/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.code;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.*;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.services.common.code.fixture.IgnoredCodeType;
import org.eclipse.scout.rt.shared.services.common.code.fixture.TestCodeType1;
import org.eclipse.scout.rt.shared.services.common.code.fixture.TestCodeType3;
import org.eclipse.scout.rt.shared.services.common.code.fixture.TestCodeType4;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * Tests for {@link CodeTypeProducer}
 */
public class CodeTypeRegistrationTest {

  @Test
  public void testGetCodeTypesWithBeans() {
    assertNotNull(BEANS.get(TestCodeType1.class));
    assertNull(BEANS.opt(IgnoredCodeType.class));
  }

  @Test
  public void testReplace() {
    MatcherAssert.assertThat(BEANS.get(TestCodeType3.class), instanceOf(TestCodeType4.class));
    MatcherAssert.assertThat(BEANS.get(TestCodeType4.class), instanceOf(TestCodeType4.class));
  }
}
