/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.code;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.filter.BeanClassFilter;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.platform.util.CompareUtility;
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
    public boolean accept(IClassInfo ci) {
      return super.accept(ci) && CompareUtility.notEquals(ci.name(), TestCodeType1.class.getName());
    }
  }

}
