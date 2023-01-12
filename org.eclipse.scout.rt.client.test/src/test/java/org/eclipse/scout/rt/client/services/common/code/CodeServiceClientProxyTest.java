/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.common.code;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.services.common.code.fixture.CompanyRatingCodeType;
import org.eclipse.scout.rt.client.services.common.code.fixture.CompanyTypeCodeType;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.cache.ICacheBuilder;
import org.eclipse.scout.rt.platform.internal.BeanInstanceUtil;
import org.eclipse.scout.rt.shared.services.common.code.CodeService;
import org.eclipse.scout.rt.shared.services.common.code.CodeTypeCacheKey;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JUnit test class for {@link CodeService} client proxy.
 *
 * @since Scout 4.1-M2
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class CodeServiceClientProxyTest {

  private CodeService m_service;

  @Before
  public void setUp() {
    m_service = createServiceUnderTest();
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

      @Override
      protected ICacheBuilder<CodeTypeCacheKey, ICodeType<?, ?>> createCacheBuilder() {
        return super.createCacheBuilder()
            .withCacheId(CODE_SERVICE_CACHE_ID + ".for.test")
            .withReplaceIfExists(true);
      }
    };
    BeanInstanceUtil.initializeBeanInstance(serviceUnderTest);
    return serviceUnderTest;
  }

  /**
   * Test method for {@link CodeService#getCodeType(Class)}.
   */
  @Test
  public void testGetCodeType() {
    CompanyRatingCodeType ct1 = m_service.getCodeType(CompanyRatingCodeType.class);
    assertNotNull(ct1);

    CompanyRatingCodeType ct2 = m_service.getCodeType(CompanyRatingCodeType.class);
    assertNotNull(ct2);
    assertSame(ct1, ct2);

    CompanyTypeCodeType ct3 = m_service.getCodeType(CompanyTypeCodeType.class);
    assertNotNull(ct3);
  }

  /**
   * Test method for {@link CodeService#getAllCodeTypes()}.
   */
  @Test
  public void testGetAllCodeTypes() {
    List<ICodeType<?, ?>> codeTypes = m_service.getAllCodeTypes();
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

    codeTypes = m_service.getAllCodeTypes();
    assertEquals("size", 2, codeTypes.size());
  }

  /**
   * Test method for {@link CodeService#findCodeTypeById(Object)}.
   */
  @Test
  public void testFindCodeTypeById() {
    //Preload the code, as a client application would do in ClientSession#execLoadSession():
    List<ICodeType<?, ?>> codeTypes = m_service.getAllCodeTypes();
    assertEquals("size", 2, codeTypes.size());

    ICodeType<Long, ?> companyTypeCodeType = m_service.findCodeTypeById(CompanyTypeCodeType.ID);
    assertEquals("CompanyTypeCodeType class", CompanyTypeCodeType.class, companyTypeCodeType.getClass());

    ICodeType<Long, ?> companyRatingCodeType = m_service.findCodeTypeById(CompanyRatingCodeType.ID);
    assertEquals("CompanyRatingCodeType 2 class", CompanyRatingCodeType.class, companyRatingCodeType.getClass());

    ICodeType<Long, ?> companyRatingCodeType2 = m_service.findCodeTypeById(CompanyRatingCodeType.ID);
    assertEquals("CompanyRatingCodeType class", CompanyRatingCodeType.class, companyRatingCodeType2.getClass());
    assertSame("CompanyRatingCodeType classes are the same", companyRatingCodeType, companyRatingCodeType2);
  }
}
