/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.code;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.services.common.code.fixture.IgnoredCodeType;
import org.eclipse.scout.rt.shared.services.common.code.fixture.TestCodeType1;
import org.eclipse.scout.rt.shared.services.common.code.fixture.TestCodeType3;
import org.eclipse.scout.rt.shared.services.common.code.fixture.TestCodeType4;
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
    assertThat(BEANS.get(TestCodeType3.class), instanceOf(TestCodeType4.class));
    assertThat(BEANS.get(TestCodeType4.class), instanceOf(TestCodeType4.class));
  }

}
