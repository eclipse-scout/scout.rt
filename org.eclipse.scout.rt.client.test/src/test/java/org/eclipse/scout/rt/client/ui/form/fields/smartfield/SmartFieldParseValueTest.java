///*******************************************************************************
// * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *     BSI Business Systems Integration AG - initial API and implementation
// ******************************************************************************/
//package org.eclipse.scout.rt.client.ui.form.fields.smartfield;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertNull;
//import static org.junit.Assert.assertSame;
//import static org.junit.Assert.assertTrue;
//
//import java.util.List;
//
//import org.eclipse.scout.rt.client.context.ClientRunContexts;
//import org.eclipse.scout.rt.client.job.ModelJobs;
//import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
//import org.eclipse.scout.rt.client.ui.basic.table.ITable;
//import org.eclipse.scout.rt.platform.BeanMetaData;
//import org.eclipse.scout.rt.platform.IBean;
//import org.eclipse.scout.rt.platform.util.Assertions;
//import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
//import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
//import org.eclipse.scout.rt.shared.services.lookup.ILookupService;
//import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
//import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
//import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
//import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
//import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
//import org.eclipse.scout.rt.testing.shared.TestingUtility;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//@RunWith(ClientTestRunner.class)
//@RunWithSubject("default")
//@RunWithClientSession(TestEnvironmentClientSession.class)
//public class SmartFieldParseValueTest {
//
//  private static List<IBean<?>> m_beans;
//
//  private AbstractSmartField<Long> m_smartField;
//
//  @BeforeClass
//  public static void beforeClass() throws Exception {
//    m_beans = TestingUtility.registerBeans(new BeanMetaData(P_LookupCall.class));
//  }
//
//  @AfterClass
//  public static void afterClass() {
//    TestingUtility.unregisterBeans(m_beans);
//  }
//
//  @Before
//  public void setUp() {
//    m_smartField = new SmartField();
//    m_smartField.registerProposalChooserInternal();
//    LookupRows.ROW_1.withEnabled(true);
//  }
//
//  /**
//   * Tests the case where only a single proposal matches the seachText and the proposal is accepted.
//   */
//  @Test
//  public void testSingleMatch() {
//    testMatch("a", 1L, "aName", 1, false, false);
//  }
//
//  /**
//   * Expect the proposal chooser to be open when multiple matches have been found.
//   */
//  @Test
//  public void testMultiMatch() {
//    testMatch("b", 0L, null, 2, true, true);
//  }
//
//  /**
//   * Expect the proposal chooser to be open when no match has been found.
//   */
//  @Test
//  public void testNoMatch() {
//    testMatch("c", 0L, null, 0, true, true);
//  }
//
//  @Test
//  public void testSetValue() throws Exception {
//    m_smartField.setValue(1L);
//    assertTrue(m_smartField.isCurrentLookupRowSet());
//    assertEquals(1L, m_smartField.getValue().longValue());
//    assertEquals("aName", m_smartField.getDisplayText());
//  }
//
//  @Test
//  public void testSetValue_MustChangeDisplayText() throws Exception {
//    m_smartField.setCurrentLookupRow(LookupRows.ROW_1);
//    m_smartField.setValue(1L);
//    assertEquals("aName", m_smartField.getDisplayText());
//    m_smartField.setValue(2L);
//    assertEquals("bName1", m_smartField.getDisplayText());
//  }
//
//  @Test
//  public void testParseAndSetValue() throws Exception {
//    m_smartField.parseAndSetValue("aName");
//    assertTrue(m_smartField.isCurrentLookupRowSet());
//    assertEquals(1L, m_smartField.getValue().longValue());
//    assertEquals("aName", m_smartField.getDisplayText());
//  }
//
//  @Test
//  public void testParseAndSetValue_InvalidValue() throws Exception {
//    m_smartField.parseAndSetValue("FooBar");
//    assertFalse(m_smartField.isCurrentLookupRowSet());
//    assertNotNull(m_smartField.getErrorStatus());
//
//    // When value becomes valid again, error status must be removed
//    m_smartField.parseAndSetValue("aName");
//    assertNull(m_smartField.getErrorStatus());
//  }
//
//  /**
//   * This method deals with the async nature of the proposal chooser
//   */
//  void testMatch(String searchText, Long expectedValue, String expectedDisplayText, int expectedNumProposals,
//      boolean expectedProposalChooserOpen, boolean expectValidationError) {
//    m_smartField.getUIFacade().openProposalChooserFromUI(searchText, false, false);
//    waitUntilLookupRowsLoaded();
//
//    if (expectedNumProposals > 0) {
//      assertTrue(m_smartField.isProposalChooserRegistered());
//      assertEquals(expectedNumProposals, getProposalTableRowCount());
//    }
//    assertEquals(searchText, m_smartField.getDisplayText());
//    assertEquals(null, m_smartField.getValue());
//
//    m_smartField.getUIFacade().acceptProposalFromUI(searchText, true, false);
//    assertEquals(expectedProposalChooserOpen, m_smartField.isProposalChooserRegistered());
//
//    if (expectValidationError) {
//      assertFalse(m_smartField.getErrorStatus().isOK());
//      assertEquals(searchText, m_smartField.getDisplayText());
//      assertEquals(null, m_smartField.getValue());
//      assertFalse(m_smartField.isCurrentLookupRowSet());
//    }
//    else {
//      assertNull(m_smartField.getErrorStatus());
//      assertEquals(expectedDisplayText, m_smartField.getDisplayText());
//      assertEquals(expectedValue, m_smartField.getValue());
//      assertTrue(m_smartField.isCurrentLookupRowSet());
//    }
//  }
//
//  /**
//   * Test case for #163567. When a SmartField has the following proposals:
//   * <ul>
//   * <li>foo</li>
//   * <li>fooBar</li>
//   * </ul>
//   * When the user clicks on "foo" in the proposalChooser and then clicks into another field acceptProposal is called
//   * with "foo". In that case (displayText == text of current lookup row) the smart-field should not lookup rows! It
//   * would result in a validation error because the search-term "foo" is not unique and would match more than 1
//   * proposal, which results in a validation error.
//   */
//  @Test
//  public void testHandleAcceptByDisplayText() throws Exception {
//    ILookupRow<Long> currentLookupRow = LookupRows.ROW_1;
//    String displayText = "aName";
//    m_smartField.setCurrentLookupRow(currentLookupRow);
//    boolean openProposalChooser = m_smartField.handleAcceptByDisplayText(displayText);
//    assertFalse(openProposalChooser);
//    assertSame(currentLookupRow, m_smartField.getLookupRow());
//    assertNull(m_smartField.getErrorStatus());
//  }
//
//  /**
//   * Test case for #172835. When one of the lookup rows is disabled and the user types accepts exactly that row by
//   * display text, the row cannot be accepted.
//   */
//  @Test
//  public void testHandleAcceptByDisplayText_InactiveLookupRow() throws Exception {
//    String displayText = "aName";
//    LookupRows.ROW_1.withEnabled(false);
//    boolean openProposalChooser = m_smartField.handleAcceptByDisplayText(displayText);
//    assertTrue(openProposalChooser);
//    assertNull(m_smartField.getLookupRow());
//    assertNotNull(m_smartField.getErrorStatus());
//  }
//
//  int getProposalTableRowCount() {
//    return ((ITable) m_smartField.getProposalChooser().getModel()).getRowCount();
//  }
//
//  private static class SmartField extends AbstractSmartField<Long> {
//    @Override
//    protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
//      return P_LookupCall.class;
//    }
//  }
//
//  public static class P_LookupCall extends LookupCall<Long> {
//
//    private static final long serialVersionUID = 1;
//
//    @Override
//    protected ILookupService<Long> createLookupService() {
//      return new P_LookupService();
//    }
//  }
//
//  public static class P_LookupService implements ILookupService<Long> {
//
//    @Override
//    public List<? extends ILookupRow<Long>> getDataByKey(ILookupCall<Long> call) {
//      return LookupRows.getRowsByKey(call.getKey());
//    }
//
//    @Override
//    public List<? extends ILookupRow<Long>> getDataByText(ILookupCall<Long> call) {
//      return LookupRows.getRowsByText(call.getText());
//    }
//
//    @Override
//    public List<? extends ILookupRow<Long>> getDataByAll(ILookupCall<Long> call) {
//      return null;
//    }
//
//    @Override
//    public List<? extends ILookupRow<Long>> getDataByRec(ILookupCall<Long> call) {
//      return null;
//    }
//  }
//
//  /**
//   * Waits for at most 30s until lookup rows are loaded.
//   */
//  private static void waitUntilLookupRowsLoaded() {
//    Assertions.assertTrue(ModelJobs.isModelThread(), "must be invoked from model thread");
//
//    // Wait until asynchronous load of lookup rows is completed and ready to be written back to the smart field.
//    JobTestUtil.waitForMinimalPermitCompetitors(ModelJobs.newInput(ClientRunContexts.copyCurrent()).getExecutionSemaphore(), 2); // 2:= 'current model job' + 'smartfield fetch model job'
//    // Yield the current model job permit, so that the lookup rows can be written into the model.
//    ModelJobs.yield();
//  }
//}
