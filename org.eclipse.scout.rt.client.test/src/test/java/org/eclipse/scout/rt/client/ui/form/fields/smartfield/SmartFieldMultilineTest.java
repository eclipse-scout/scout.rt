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

import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class SmartFieldMultilineTest {
  private static List<IBean<?>> m_beans;

  @BeforeClass
  public static void beforeClass() throws Exception {
    m_beans = TestingUtility.registerBeans(new BeanMetaData(P_SingleLookupCall.class), new BeanMetaData(P_MultiLookupCall.class));
  }

  @AfterClass
  public static void afterClass() {
    TestingUtility.unregisterBeans(m_beans);
  }

  private SingleCallSingleField m_singleCallSingleField = new SingleCallSingleField();
  private SingleCallMultiField m_singleCallMultiField = new SingleCallMultiField();
  private MultiCallSingleField m_multiCallSingleField = new MultiCallSingleField();
  private MultiCallMultiField m_multiCallMultiField = new MultiCallMultiField();

  /**
   * Tests whether new lines get replaced, if multilineText is set to false
   */
  @Test
  public void testSingleLine() {
    m_singleCallSingleField.setValue(1L);
    assertSingleLine(m_singleCallSingleField.getDisplayText());
  }

  /**
   * Tests whether field with a multiline lookupcall or multiline field gets a multiline lookup row
   */
  @Test
  public void testMultiLine() {
    m_singleCallMultiField.setValue(1L);
    assertMultiLine(m_singleCallMultiField.getDisplayText());
    m_multiCallSingleField.setValue(1L);
    assertMultiLine(m_multiCallSingleField.getDisplayText());
    m_multiCallMultiField.setValue(1L);
    assertMultiLine(m_multiCallMultiField.getDisplayText());
  }

  /**
   * Tests if the text of a multiline lookup row which is the currentLookupRow matches the displayText of the field.
   * Otherwise we'd always get a validation error for this field, even when the user has just selected a valid proposal
   * from the proposal chooser.
   */
  @Test
  public void testMultiLine_DisplayTextMatchesCurrentLookupRow() {
    m_singleCallMultiField.setValue(1L);
    assertMultiLine(m_singleCallMultiField.getDisplayText());

    m_singleCallMultiField.getUIFacade().acceptProposalFromUI("Line1 Line2", false, false);
    // if multi line texts are not handled correctly the currentLookupRow would be
    // set to null in AbstractContentAssistField#parseValueInternal()
    assertNotNull(m_singleCallMultiField.getLookupRow());
    assertEquals(4L, m_singleCallMultiField.getLookupRow().getKey().longValue());
  }

  protected void assertSingleLine(String displayText) {
    assertEquals("Line1 Line2", displayText);
  }

  protected void assertMultiLine(String displayText) {
    assertEquals("Line1\nLine2", displayText);
  }

  // single-line lookupcall in single-line field
  @ClassId("69d13d93-2f92-45a4-9892-094bd5f3b3ce")
  private static class SingleCallSingleField extends AbstractSmartField<Long> {

    @Override
    protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
      return P_SingleLookupCall.class;
    }
  }

  // single-line lookupcall in multi-line field
  @ClassId("a530d728-36aa-4ac1-82be-a118976aa65a")
  private static class SingleCallMultiField extends AbstractSmartField<Long> {

    @Override
    protected boolean getConfiguredMultilineText() {
      return true;
    }

    @Override
    protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
      return P_SingleLookupCall.class;
    }
  }

  // multi-line lookupcall in single-line field
  @ClassId("8f8379be-acac-4f6f-9e16-118305157ab0")
  private static class MultiCallSingleField extends AbstractSmartField<Long> {

    @Override
    protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
      return P_MultiLookupCall.class;
    }
  }

  // multi-line lookupcall in multi-line field
  @ClassId("d8064454-87ff-4afd-8f11-52eef4ade7dd")
  private static class MultiCallMultiField extends AbstractSmartField<Long> {

    @Override
    protected boolean getConfiguredMultilineText() {
      return true;
    }

    @Override
    protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
      return P_MultiLookupCall.class;
    }
  }

  @ClassId("43949094-ddb9-47f2-9bdf-4f208e1ed499")
  public static class P_SingleLookupCall extends LookupCall<Long> {
    private static final long serialVersionUID = -7536271824820806283L;

    @Override
    protected ILookupService<Long> createLookupService() {
      return new P_LookupService();
    }
  }

  @ClassId("93091218-02e3-4cfa-996d-2b2a1fa97495")
  public static class P_MultiLookupCall extends LookupCall<Long> {
    private static final long serialVersionUID = -7536271824820806283L;

    @Override
    protected boolean getConfiguredMultilineText() {
      return true;
    }

    @Override
    protected ILookupService<Long> createLookupService() {
      return new P_LookupService();
    }
  }

  public static class P_LookupService implements ILookupService<Long> {

    @Override
    public List<? extends ILookupRow<Long>> getDataByKey(ILookupCall<Long> call) {
      return LookupRows.multiLineRow();
    }

    @Override
    public List<? extends ILookupRow<Long>> getDataByText(ILookupCall<Long> call) {
      return LookupRows.multiLineRow();
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
}
