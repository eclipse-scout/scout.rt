/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanMetaDataFacotry;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
@SuppressWarnings("deprecation")
public class ProposalFieldParseValueTest {

  private static List<IBean<?>> m_beans;

  private ProposalField m_proposalField = new ProposalField();

  @BeforeClass
  public static void beforeClass() throws Exception {
    m_beans = TestingUtility.registerBeans(BEANS.get(IBeanMetaDataFacotry.class).create(P_LookupCall.class));
  }

  @AfterClass
  public static void afterClass() {
    TestingUtility.unregisterBeans(m_beans);
  }

  @Before
  public void setUp() throws ProcessingException {
    m_proposalField.registerProposalChooserInternal();
  }

  @Test
  public void testSingleMatch() throws ProcessingException {
    // single match
    m_proposalField.getUIFacadeLegacy().setTextFromUI("a");
    assertEquals("a", m_proposalField.getValue());
    // select proposal
    assertNotNull(m_proposalField.getProposalChooser());
    m_proposalField.getProposalChooser().forceProposalSelection();
    m_proposalField.acceptProposal();
    assertEquals("aName", m_proposalField.getValue());
    assertNull(m_proposalField.getProposalChooser());

  }

  @Test
  public void testMultiMatch() throws ProcessingException {
    // match with two elements
    m_proposalField.getUIFacadeLegacy().setTextFromUI("b");
    assertEquals("b", m_proposalField.getValue());
    assertNotNull(m_proposalField.getProposalChooser());
    assertEquals(2, m_proposalField.getProposalChooser().getSearchResult().getLookupRows().size());
    // select first
    m_proposalField.getProposalChooser().forceProposalSelection();
    // close the proposal form
    m_proposalField.acceptProposal();
    assertNull(m_proposalField.getProposalChooser());
    assertEquals("aName", m_proposalField.getValue());
  }

  @Test
  public void testNoMatch() throws ProcessingException {
    // single match
    m_proposalField.getUIFacade().acceptProposalFromUI("c");
    assertEquals("c", m_proposalField.getValue());
    assertNull(m_proposalField.getProposalChooser());

  }

  private static class ProposalField extends AbstractProposalField<Long> {
    @Override
    protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
      return P_LookupCall.class;
    }
  }

  @ClassId("ad1b3fc1-4b33-4c7d-9506-a6e36618f77f")
  public static class P_LookupCall extends LookupCall<Long> {

    private static final long serialVersionUID = 1L;

    @Override
    protected ILookupService<Long> createLookupService() {
      return new P_LookupService();
    }

  }

  public static class P_LookupService implements ILookupService<Long> {

    @Override
    public List<? extends ILookupRow<Long>> getDataByKey(ILookupCall<Long> call) throws ProcessingException {
      return null;
    }

    @Override
    public List<? extends ILookupRow<Long>> getDataByText(ILookupCall<Long> call) throws ProcessingException {
      if ("a*".equals(call.getText())) {
        return CollectionUtility.arrayList(new LookupRow<Long>(1L, "aName"));
      }
      if ("b*".equals(call.getText())) {
        return CollectionUtility.arrayList(new LookupRow<Long>(1L, "aName"), new LookupRow<Long>(2L, "bName"));
      }
      return Collections.emptyList();
    }

    @Override
    public List<? extends ILookupRow<Long>> getDataByAll(ILookupCall<Long> call) throws ProcessingException {
      return null;
    }

    @Override
    public List<? extends ILookupRow<Long>> getDataByRec(ILookupCall<Long> call) throws ProcessingException {
      return null;
    }

  }
}
