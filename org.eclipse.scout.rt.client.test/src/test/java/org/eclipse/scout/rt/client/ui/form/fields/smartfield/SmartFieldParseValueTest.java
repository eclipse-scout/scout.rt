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

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
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
  public static void beforeClass() {
    m_beans = BeanTestingHelper.get().registerBeans(new BeanMetaData(P_LookupCall.class));
  }

  @AfterClass
  public static void afterClass() {
    BeanTestingHelper.get().unregisterBeans(m_beans);
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
  public void testSetValue() {
    m_smartField.setValue(1L);
    assertTrue(isCurrentLookupRowSet(m_smartField));
    assertEquals(1L, m_smartField.getValue().longValue());
    assertEquals("aName", m_smartField.getDisplayText());
  }

  @Test
  public void testSetValue_MustChangeDisplayText() {
    m_smartField.setLookupRow(LookupRows.ROW_1);
    m_smartField.setValue(1L);
    assertEquals("aName", m_smartField.getDisplayText());
    m_smartField.setValue(2L);
    assertEquals("bName1", m_smartField.getDisplayText());
  }

  @Test
  public void testParseAndSetValue() {
    m_smartField.parseAndSetValue("aName");
    assertTrue(isCurrentLookupRowSet(m_smartField));
    assertEquals(1L, m_smartField.getValue().longValue());
    assertEquals("aName", m_smartField.getDisplayText());
  }

  @Test
  public void testParseAndSetValue_InvalidValue() {
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
