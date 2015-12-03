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

  private SmartField m_smartField = new SmartField();

  @BeforeClass
  public static void beforeClass() throws Exception {
    m_beans = TestingUtility.registerBeans(new BeanMetaData(P_LookupCall.class));
  }

  @AfterClass
  public static void afterClass() {
    TestingUtility.unregisterBeans(m_beans);
  }

  /**
   * Tests whether new lines get replaced, if multilineText is set to false
   */
  @Test
  public void testSingleLine() {
    m_smartField.setValue(1L);
    assertEquals("Line1 Line2", m_smartField.getDisplayText());
  }

  /**
   * Tests whether new lines don't get replaced, if multilineText is set to true
   */
  @Test
  public void testMultiLine() {
    m_smartField.setMultilineText(true);
    m_smartField.setValue(1L);
    assertEquals("Line1\nLine2", m_smartField.getDisplayText());
  }

  /**
   * Tests if the text of a multiline lookup row which is the currentLookupRow matches the displayText of the field.
   * Otherwise we'd always get a validation error for this field, even when the user has just selected a valid proposal
   * from the proposal chooser.
   */
  @Test
  public void testMultiLine_DisplayTextMatchesCurrentLookupRow() {
    m_smartField.setMultilineText(true);
    m_smartField.setValue(1L);
    assertEquals("Line1\nLine2", m_smartField.getDisplayText());

    m_smartField.getUIFacade().acceptProposalFromUI("Line1 Line2", false, false);
    // if multi line texts are not handled correctly the currentLookupRow would be
    // set to null in AbstractContentAssistField#parseValueInternal()
    assertNotNull(m_smartField.getCurrentLookupRow());
    assertEquals(4L, m_smartField.getCurrentLookupRow().getKey().longValue());
  }

  private static class SmartField extends AbstractSmartField<Long> {
    @Override
    protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
      return P_LookupCall.class;
    }
  }

  @ClassId("43949094-ddb9-47f2-9bdf-4f208e1ed499")
  public static class P_LookupCall extends LookupCall<Long> {

    private static final long serialVersionUID = -7536271824820806283L;

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
