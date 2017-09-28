/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
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
public class SmartFieldParseValueTest {

  private static List<IBean<?>> m_beans;

  private AbstractSmartField<Long> m_smartField;

  @BeforeClass
  public static void beforeClass() throws Exception {
    m_beans = TestingUtility.registerBeans(new BeanMetaData(P_LookupCall.class));
  }

  @AfterClass
  public static void afterClass() {
    TestingUtility.unregisterBeans(m_beans);
  }

  @Before
  public void setUp() {
    m_smartField = new SmartField();
    LookupRows.ROW_1.withEnabled(true);
  }

  private boolean isCurrentLookupRowSet(ISmartField<?> smartField) {
    return smartField.getLookupRow() != null;
  }

  @Test
  public void testSetValue() throws Exception {
    m_smartField.setValue(1L);
    assertTrue(isCurrentLookupRowSet(m_smartField));
    assertEquals(1L, m_smartField.getValue().longValue());
    assertEquals("aName", m_smartField.getDisplayText());
  }

  @Test
  public void testSetValue_MustChangeDisplayText() throws Exception {
    m_smartField.setLookupRow(LookupRows.ROW_1);
    m_smartField.setValue(1L);
    assertEquals("aName", m_smartField.getDisplayText());
    m_smartField.setValue(2L);
    assertEquals("bName1", m_smartField.getDisplayText());
  }

  @Test
  public void testParseAndSetValue() throws Exception {
    m_smartField.parseAndSetValue("aName");
    assertTrue(isCurrentLookupRowSet(m_smartField));
    assertEquals(1L, m_smartField.getValue().longValue());
    assertEquals("aName", m_smartField.getDisplayText());
  }

  @Test
  public void testParseAndSetValue_InvalidValue() throws Exception {
    m_smartField.parseAndSetValue("FooBar");
    assertFalse(isCurrentLookupRowSet(m_smartField));
    assertNotNull(m_smartField.getErrorStatus());

    // When value becomes valid again, error status must be removed
    m_smartField.parseAndSetValue("aName");
    assertNull(m_smartField.getErrorStatus());
  }

  private static class SmartField extends AbstractSmartField<Long> {
    @Override
    protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
      return P_LookupCall.class;
    }
  }

  public static class P_LookupCall extends LookupCall<Long> {

    private static final long serialVersionUID = 1;

    @Override
    protected ILookupService<Long> createLookupService() {
      return new P_LookupService();
    }
  }

  public static class P_LookupService implements ILookupService<Long> {

    @Override
    public List<? extends ILookupRow<Long>> getDataByKey(ILookupCall<Long> call) {
      return LookupRows.getRowsByKey(call.getKey());
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

}
