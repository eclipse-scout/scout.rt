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
package org.eclipse.scout.rt.client.services.common.code;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.services.common.code.fixture.CompanyRatingCodeType;
import org.eclipse.scout.rt.client.services.common.code.fixture.CompanyTypeCodeType;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.internal.BeanInstanceUtil;
import org.eclipse.scout.rt.shared.services.common.code.CodeService;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JUnit test class for {@link CodeServiceClientProxy}.
 *
 * @since Scout 4.1-M2
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class CodeServiceClientProxyTest {

  private static final String CLIENT_CODE_TYPE_PREFIX = "org.eclipse.scout.rt.client";
  private CodeService m_service;

  @Before
  public void setUp() throws Exception {
    m_service = createServiceUnderTest();
  }

  /**
   * Test method for
   * {@link org.eclipse.scout.rt.client.services.common.code.CodeServiceClientProxy#getCodeType(java.lang.Class)}.
   */
  @Test
  public void testGetCodeType() throws Exception {
    CompanyRatingCodeType ct1 = m_service.getCodeType(CompanyRatingCodeType.class);
    assertNotNull(ct1);

    CompanyRatingCodeType ct2 = m_service.getCodeType(CompanyRatingCodeType.class);
    assertNotNull(ct2);
    assertTrue(ct1 == ct2);

    CompanyTypeCodeType ct3 = m_service.getCodeType(CompanyTypeCodeType.class);
    assertNotNull(ct3);
  }

  /**
   * Test method for
   * {@link org.eclipse.scout.rt.client.services.common.code.CodeServiceClientProxy#getAllCodeTypes(java.lang.String)}.
   */
  @Test
  public void testGetAllCodeTypes() throws Exception {
    List<ICodeType<?, ?>> codeTypes = m_service.getAllCodeTypes("");
    assertEquals("size", 2, codeTypes.size());
    ICodeType<?, ?> codeType = codeTypes.get(0);
    ICodeType<?, ?> companyRatingCodeType;
    ICodeType<?, ?> companyTypeCodeType;
    if (codeType instanceof CompanyRatingCodeType) {
      companyRatingCodeType = codeType;
      companyTypeCodeType = codeTypes.get(1);
    }
    else {
      companyRatingCodeType = codeTypes.get(1);
      companyTypeCodeType = codeType;
    }
    assertEquals("CompanyRatingCodeType class", CompanyRatingCodeType.class, companyRatingCodeType.getClass());
    assertEquals("CompanyTypeCodeType class", CompanyTypeCodeType.class, companyTypeCodeType.getClass());

    codeTypes = m_service.getAllCodeTypes("");
    assertEquals("size", 2, codeTypes.size());

    codeTypes = m_service.getAllCodeTypes(CLIENT_CODE_TYPE_PREFIX);
    assertEquals("size", 2, codeTypes.size());
  }

  /**
   * Test method for
   * {@link org.eclipse.scout.rt.client.services.common.code.CodeServiceClientProxy#findCodeTypeById(java.lang.Object)}.
   */
  @Test
  public void testFindCodeTypeById() throws Exception {
    //Preload the code, as a client application would do in ClientSession#execLoadSession():
    List<ICodeType<?, ?>> codeTypes = m_service.getAllCodeTypes("");
    assertEquals("size", 2, codeTypes.size());

    ICodeType<Long, ?> companyTypeCodeType = m_service.findCodeTypeById(CompanyTypeCodeType.ID);
    assertEquals("CompanyTypeCodeType class", CompanyTypeCodeType.class, companyTypeCodeType.getClass());

    ICodeType<Long, ?> companyRatingCodeType = m_service.findCodeTypeById(CompanyRatingCodeType.ID);
    assertEquals("CompanyRatingCodeType 2 class", CompanyRatingCodeType.class, companyRatingCodeType.getClass());

    ICodeType<Long, ?> companyRatingCodeType2 = m_service.findCodeTypeById(CompanyRatingCodeType.ID);
    assertEquals("CompanyRatingCodeType class", CompanyRatingCodeType.class, companyRatingCodeType2.getClass());
    assertTrue("CompanyRatingCodeType classes are the same", companyRatingCodeType == companyRatingCodeType2);
  }

  private static CodeService createServiceUnderTest() {
    CodeService serviceUnderTest = new CodeService() {

      @Override
      public Set<Class<? extends ICodeType<?, ?>>> getAllCodeTypeClasses() {
        Set<Class<? extends ICodeType<?, ?>>> hashSet = new HashSet<>();
        hashSet.add(CompanyRatingCodeType.class);
        hashSet.add(CompanyTypeCodeType.class);
        return hashSet;
      }
    };
    BeanInstanceUtil.initializeBeanInstance(serviceUnderTest);
    return serviceUnderTest;
  }
}
