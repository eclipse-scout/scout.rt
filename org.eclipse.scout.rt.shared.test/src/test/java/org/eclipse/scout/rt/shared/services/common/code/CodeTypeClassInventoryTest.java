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

import static org.junit.Assert.*;

import java.util.Collection;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.filter.BeanClassFilter;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.shared.services.common.code.fixture.IgnoredCodeType;
import org.eclipse.scout.rt.shared.services.common.code.fixture.TestCodeType1;
import org.eclipse.scout.rt.shared.services.common.code.fixture.TestCodeType2;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link CodeTypeClassInventory}
 */
@RunWith(PlatformTestRunner.class)
public class CodeTypeClassInventoryTest {

  private CodeTypeClassInventory m_service;

  @Before
  public void before() {
    m_service = BEANS.get(CodeTypeClassInventory.class);
  }

  /**
   * Tests that a code type class is found by {@link CodeTypeClassInventory#getAllCodeTypeClasses)}
   */
  @Test
  public void testCodeTypeClasses() {
    Collection<Class<? extends ICodeType<?, ?>>> allCodeTypes = m_service.getClasses();
    assertTrue(allCodeTypes.contains(TestCodeType1.class));
    assertFalse(allCodeTypes.contains(IgnoredCodeType.class));
    assertFalse(allCodeTypes.contains(AbstractCodeType.class));
  }

  @Test
  public void testCodeTypeClass_IgnoredName() {
    CodeTypeClassInventory testService = new CodeTypeClassInventory(new IgnoreTestCodeType1Filter());
    Collection<Class<? extends ICodeType<?, ?>>> allCodeTypes = testService.getClasses();
    assertFalse(allCodeTypes.contains(TestCodeType1.class));
    assertTrue(allCodeTypes.contains(TestCodeType2.class));
  }

  /**
   * Accept everything except TestCodeType1
   */
  static class IgnoreTestCodeType1Filter extends BeanClassFilter {

    @Override
    public boolean test(IClassInfo ci) {
      return super.test(ci) && ObjectUtility.notEquals(ci.name(), TestCodeType1.class.getName());
    }
  }
}
