/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
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
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.services.common.code.fixture.CompanyRatingCodeType;
import org.eclipse.scout.rt.client.services.common.code.fixture.CompanyTypeCodeType;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.shared.services.common.code.ICodeService;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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
  private ICodeService m_remoteService;
  private CodeServiceClientProxy m_service;

  @Before
  public void setUp() throws Exception {
    m_remoteService = createRemoteService();
    m_service = createServiceUnderTest(m_remoteService);
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

    verify(m_remoteService, times(1)).getCodeType(CompanyRatingCodeType.class);
    verify(m_remoteService, times(1)).getCodeType(CompanyTypeCodeType.class);
    verifyNoMoreInteractions(m_remoteService);
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

    verify(m_remoteService, times(1)).getAllCodeTypeClasses("");
    verify(m_remoteService, times(1)).getCodeTypes(anyListOfCodeTypes());
    verify(m_remoteService, times(1)).getAllCodeTypeClasses(CLIENT_CODE_TYPE_PREFIX);
    verifyNoMoreInteractions(m_remoteService);
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

    verify(m_remoteService, times(1)).getAllCodeTypeClasses("");
    verify(m_remoteService, times(1)).getCodeTypes(anyListOfCodeTypes());
    verifyNoMoreInteractions(m_remoteService);
  }

  private static ICodeService createRemoteService() throws ProcessingException {
    ICodeService remoteService = Mockito.mock(ICodeService.class);

    //getAllCodeTypeClasses:
    Set<Class<? extends ICodeType<?, ?>>> hashSet = new HashSet<>();
    hashSet.add(CompanyRatingCodeType.class);
    hashSet.add(CompanyTypeCodeType.class);
    when(remoteService.getAllCodeTypeClasses("")).thenReturn(hashSet);
    when(remoteService.getAllCodeTypeClasses("org.eclipse.scout.rt.client")).thenReturn(hashSet);

    //getCodeType:
    when(remoteService.getCodeType(CompanyRatingCodeType.class)).thenReturn(new CompanyRatingCodeType());
    when(remoteService.getCodeType(CompanyTypeCodeType.class)).thenReturn(new CompanyTypeCodeType());

    //getCodeTypes(List):
    when(remoteService.getCodeTypes(anyListOfCodeTypes())).thenAnswer(new Answer() {

      @Override
      public Object answer(InvocationOnMock invocation) throws ProcessingException {
        Object[] args = invocation.getArguments();
        List<ICodeType<?, ?>> result = new ArrayList<ICodeType<?, ?>>();
        @SuppressWarnings("unchecked")
        List<Class<? extends ICodeType<?, ?>>> list = (List<Class<? extends ICodeType<?, ?>>>) args[0];
        for (Class<? extends ICodeType<?, ?>> codeTypeClass : list) {
          if (CompanyRatingCodeType.class.equals(codeTypeClass)) {
            result.add(new CompanyRatingCodeType());
          }
          else if (CompanyTypeCodeType.class.equals(codeTypeClass)) {
            result.add(new CompanyTypeCodeType());
          }
          else {
            fail("call of RemoteService#getCodeTypes(List) with a list containing an unexpected ");
          }
        }
        return result;
      }
    });

    return remoteService;
  }

  private static CodeServiceClientProxy createServiceUnderTest(final ICodeService remoteService) {
    CodeServiceClientProxy serviceUnderTest = new CodeServiceClientProxy() {

      @Override
      protected ICodeService getRemoteService() {
        return remoteService;
      }
    };
    return serviceUnderTest;
  }

  @SuppressWarnings("unchecked")
  private static List<Class<? extends ICodeType<?, ?>>> anyListOfCodeTypes() {
    return (List<Class<? extends ICodeType<?, ?>>>) any();
  }
}
