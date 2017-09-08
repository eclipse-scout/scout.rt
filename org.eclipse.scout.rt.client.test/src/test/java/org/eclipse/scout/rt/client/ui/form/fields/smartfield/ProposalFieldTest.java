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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class ProposalFieldTest {

  private static List<IBean<?>> m_beans;

  @BeforeClass
  public static void beforeClass() throws Exception {
    m_beans = TestingUtility.registerBeans(new BeanMetaData(P_LookupCall.class));
  }

  @AfterClass
  public static void afterClass() {
    TestingUtility.unregisterBeans(m_beans);
  }

  private ProposalField m_proposalField = new ProposalField();

  @Before
  public void setUp() {
    m_proposalField = new ProposalField();
  }

  /**
   * When no proposal matches the searchText, the ProposalField uses that searchText as value.
   */
  @Test
  public void testNoMatch() {
    testMatch("c", "c", 0);
  }

  /**
   * Proposal for "a" returns one match. Other than the SmartField the ProposalField does not automatically set a single
   * proposal match as value, it only sets the searchText as value.
   */
  @Test
  public void testSingleMatch() {
    testMatch("a", "a", 1);
  }

  /**
   * Proposal for "b" returns two matches. When we accept the proposal for "b" the ProposalField simply sets the value
   * (other than the SmartField which throws a VetoException in that case).
   */
  @Test
  public void testMultiMatch() {
    testMatch("b", "b", 2);
  }

  @Test
  public void testSetLookupRow() {
    m_proposalField.setValueByLookupRow(LookupRows.ROW_2);
    assertEquals("bName1", m_proposalField.getDisplayText());
    assertEquals("bName1", m_proposalField.getValue());
    assertNotNull(m_proposalField.getLookupRow());
  }

  @Test
  public void testValueAndDisplayText() {
    m_proposalField.setValueAsString("customText123");
    assertEquals("customText123", m_proposalField.getDisplayText());
    assertEquals("customText123", m_proposalField.getValue());
  }

  @Test
  public void testLookupRowWithNullText() throws Exception {
    LookupRow<Long> nullLookupRow = new LookupRow<Long>(1L, null);
    m_proposalField.setLookupRow(nullLookupRow);
    assertEquals("", m_proposalField.getDisplayText());
  }

  @Test
  public void testTrimText() throws Exception {
    m_proposalField.setTrimText(false);
    m_proposalField.setValueAsString(" a ");
    assertEquals(" a ", m_proposalField.getValue());
    m_proposalField.setTrimText(true);
    assertEquals("a", m_proposalField.getValue());
    m_proposalField.setValueAsString(" b ");
    assertEquals("b", m_proposalField.getValue());
  }

  @Test
  public void testMaxLength_TextTooLong() throws Exception {
    m_proposalField.setMaxLength(32);
    m_proposalField.setValueAsString("1234567890");
    assertEquals("1234567890", m_proposalField.getValue());
    m_proposalField.setMaxLength(8);
    assertEquals("12345678", m_proposalField.getValue());
    m_proposalField.setValueAsString("1234567abc");
    assertEquals("1234567a", m_proposalField.getValue());
  }

  /**
   * This method deals with the async nature of the proposal chooser
   */
  void testMatch(String searchText, String expectedValue, int expectedNumProposals) {
    m_proposalField.lookupByText(searchText);
    waitUntilLookupRowsLoaded();

    boolean proposalChooserOpen = expectedNumProposals > 0;
    if (proposalChooserOpen) {
      assertEquals(expectedNumProposals, getLookupRowsCount());
    }

    m_proposalField.setValueAsString(searchText);
    assertEquals(expectedValue, m_proposalField.getDisplayText());
    assertEquals(expectedValue, m_proposalField.getValue());

    // since we've never clicked on a proposal, we don't expect a lookup row is set
    assertNull(m_proposalField.getLookupRow());
  }

  @Test
  public void testValueAsLookupKey() {
    m_proposalField.setLookupRow(new LookupRow<Long>(1L, "aName"));
    assertEquals(Long.valueOf(1L), m_proposalField.getLookupRow().getKey());
  }

  @Test
  public void testValueAsLookupKey_NoValue() {
    m_proposalField.setLookupRow(null);
    Assert.assertNull(m_proposalField.getLookupRow());
  }

  int getLookupRowsCount() {
    return m_proposalField.getResult().getLookupRows().size();
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
    public List<? extends ILookupRow<Long>> getDataByKey(ILookupCall<Long> call) {
      return null;
    }

    @Override
    public List<? extends ILookupRow<Long>> getDataByText(ILookupCall<Long> call) {
      return LookupRows.getRowsByText(call.getText());
    }

    @Override
    public List<? extends ILookupRow<Long>> getDataByAll(ILookupCall<Long> call) {
      return null;
    }

    @Override
    public List<? extends ILookupRow<Long>> getDataByRec(ILookupCall<Long> call) {
      return null;
    }
  }

  /**
   * Waits for at most 30s until lookup rows are loaded.
   */
  private static void waitUntilLookupRowsLoaded() {
    Assertions.assertTrue(ModelJobs.isModelThread(), "must be invoked from model thread");

    // Wait until asynchronous load of lookup rows is completed and ready to be written back to the smart field.
    JobTestUtil.waitForMinimalPermitCompetitors(ModelJobs.newInput(ClientRunContexts.copyCurrent()).getExecutionSemaphore(), 2); // 2:= 'current model job' + 'smartfield fetch model job'
    // Yield the current model job permit, so that the lookup rows can be written into the model.
    ModelJobs.yield();
  }
}
