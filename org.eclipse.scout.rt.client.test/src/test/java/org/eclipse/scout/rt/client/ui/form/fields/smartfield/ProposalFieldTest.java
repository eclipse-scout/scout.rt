/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import static org.junit.Assert.*;

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
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
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
  public static void beforeClass() {
    m_beans = BeanTestingHelper.get().registerBeans(new BeanMetaData(P_LookupCall.class));
  }

  @AfterClass
  public static void afterClass() {
    BeanTestingHelper.get().unregisterBeans(m_beans);
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
  public void testLookupRowWithNullText() {
    LookupRow<Long> nullLookupRow = new LookupRow<>(1L, null);
    m_proposalField.setLookupRow(nullLookupRow);
    assertEquals("", m_proposalField.getDisplayText());
  }

  @Test
  public void testTrimText_Spaces() {
    m_proposalField.setTrimText(false);
    m_proposalField.setValueAsString(" a ");
    assertEquals(" a ", m_proposalField.getValue());
    m_proposalField.setTrimText(true);
    assertEquals("a", m_proposalField.getValue());
    m_proposalField.setValueAsString(" b ");
    assertEquals("b", m_proposalField.getValue());
  }

  @Test
  public void testMaxLength_TextTooLong() {
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
    m_proposalField.setLookupRow(new LookupRow<>(1L, "aName"));
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

  @Test
  public void testMaxLength() {
    int initialMaxLength = m_proposalField.getMaxLength();
    assertEquals(m_proposalField.getConfiguredMaxLength(), initialMaxLength);
    m_proposalField.setMaxLength(1234);
    assertEquals(1234, m_proposalField.getMaxLength());
    m_proposalField.setMaxLength(0);
    assertEquals(0, m_proposalField.getMaxLength());
    m_proposalField.setMaxLength(-2);
    assertEquals(0, m_proposalField.getMaxLength());

    // set value
    m_proposalField.setValueAsString("the clown has a red nose");
    assertEquals(null, m_proposalField.getValue());
    m_proposalField.setMaxLength(9);
    m_proposalField.setValueAsString("the clown has a red nose");
    assertEquals("the clown", m_proposalField.getValue());
    m_proposalField.setMaxLength(4);
    assertEquals("the", m_proposalField.getValue());
  }

  @Test
  public void testTrimText() {
    m_proposalField.setMultilineText(true);

    m_proposalField.setTrimText(true);
    m_proposalField.setValueAsString("  a  b  ");
    assertEquals("a  b", m_proposalField.getValue());
    m_proposalField.setValueAsString("\n  a \n b  \n");
    assertEquals("a \n b", m_proposalField.getValue());
    m_proposalField.setValueAsString(null);
    assertEquals(null, m_proposalField.getValue());

    m_proposalField.setTrimText(false);
    m_proposalField.setValueAsString("  a  b  ");
    assertEquals("  a  b  ", m_proposalField.getValue());
    m_proposalField.setValueAsString("\n  a \n b  \n");
    assertEquals("\n  a \n b  \n", m_proposalField.getValue());
    m_proposalField.setValueAsString(null);
    assertEquals(null, m_proposalField.getValue());

    // set value
    m_proposalField.setValueAsString("  a  b  ");
    assertEquals("  a  b  ", m_proposalField.getValue());
    m_proposalField.setTrimText(true);
    assertEquals("a  b", m_proposalField.getValue());
  }

  @Test
  public void testMultilineText() {
    m_proposalField.setMultilineText(false);

    m_proposalField.setValueAsString("a\n\nb");
    assertEquals("a  b", m_proposalField.getValue());
    m_proposalField.setValue(null);
    assertEquals(null, m_proposalField.getValue());

    m_proposalField.setMultilineText(true);
    m_proposalField.setValueAsString("a\n\nb");
    assertEquals("a\n\nb", m_proposalField.getValue());
    m_proposalField.setValue(null);
    assertEquals(null, m_proposalField.getValue());

    // set value
    m_proposalField.setMultilineText(true);
    m_proposalField.setValueAsString("a\nb");
    assertEquals("a\nb", m_proposalField.getValue());
    m_proposalField.setMultilineText(false);
    assertEquals("a b", m_proposalField.getValue());
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
