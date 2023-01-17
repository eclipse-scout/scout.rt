/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.listbox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 5.2
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class ListBoxInFormTest {

  private static final String LIST_BOX_FILTER_BOX_CLASS_ID = "a2e982d1-ea01-4d11-8655-d10c9935d8b9";
  private static final String LIST_BOX_ACTIVE_STATE_BOX_CLASS_ID = "2c4e4cf0-7bcf-46f1-a00f-19ecd2719fff";
  private static final String LIST_BOX_CHECKED_STATE_BOX_CLASS_ID = "e62c300f-f49d-4318-95ce-44a60558cfbf";

  private ListBoxTestForm m_form;

  @Before
  public void before() {
    m_form = new ListBoxTestForm();
  }

  @Test
  public void testListBoxClassIds() {
    assertEquals(ListBoxTestForm.FIRST_LIST_BOX_CLASS_ID, m_form.getFirstListBox().classId());
    assertEquals(ListBoxTestForm.SECOND_LIST_BOX_CLASS_ID, m_form.getSecondListBox().classId());
  }

  @Test
  public void testListBoxFilterBoxClassIds() {
    String firstClassId = m_form.getFirstListBox().getListBoxFilterBox().classId();
    String secondClassId = m_form.getSecondListBox().getListBoxFilterBox().classId();
    assertNotEquals(firstClassId, secondClassId);

    assertEquals(LIST_BOX_FILTER_BOX_CLASS_ID + ITypeWithClassId.ID_CONCAT_SYMBOL + ListBoxTestForm.FIRST_LIST_BOX_CLASS_ID, firstClassId);
    assertEquals(LIST_BOX_FILTER_BOX_CLASS_ID + ITypeWithClassId.ID_CONCAT_SYMBOL + ListBoxTestForm.SECOND_LIST_BOX_CLASS_ID, secondClassId);
  }

  @Test
  public void testListBoxFilterBoxActiveStateRadioButtonGroupClassIds() {
    String firstClassId = m_form.getFirstListBox().getListBoxFilterBox().getActiveStateRadioButtonGroup().classId();
    String secondClassId = m_form.getSecondListBox().getListBoxFilterBox().getActiveStateRadioButtonGroup().classId();
    assertNotEquals(firstClassId, secondClassId);

    assertEquals(StringUtility.join(ITypeWithClassId.ID_CONCAT_SYMBOL,
        LIST_BOX_ACTIVE_STATE_BOX_CLASS_ID,
        LIST_BOX_FILTER_BOX_CLASS_ID,
        ListBoxTestForm.FIRST_LIST_BOX_CLASS_ID), firstClassId);

    assertEquals(StringUtility.join(ITypeWithClassId.ID_CONCAT_SYMBOL,
        LIST_BOX_ACTIVE_STATE_BOX_CLASS_ID,
        LIST_BOX_FILTER_BOX_CLASS_ID,
        ListBoxTestForm.SECOND_LIST_BOX_CLASS_ID), secondClassId);
  }

  @Test
  public void testListBoxFilterBoxCheckedStateRadioButtonGroupClassIds() {
    String firstClassId = m_form.getFirstListBox().getListBoxFilterBox().getCheckedStateRadioButtonGroup().classId();
    String secondClassId = m_form.getSecondListBox().getListBoxFilterBox().getCheckedStateRadioButtonGroup().classId();
    assertNotEquals(firstClassId, secondClassId);

    assertEquals(StringUtility.join(ITypeWithClassId.ID_CONCAT_SYMBOL,
        LIST_BOX_CHECKED_STATE_BOX_CLASS_ID,
        LIST_BOX_FILTER_BOX_CLASS_ID,
        ListBoxTestForm.FIRST_LIST_BOX_CLASS_ID), firstClassId);

    assertEquals(StringUtility.join(ITypeWithClassId.ID_CONCAT_SYMBOL,
        LIST_BOX_CHECKED_STATE_BOX_CLASS_ID,
        LIST_BOX_FILTER_BOX_CLASS_ID,
        ListBoxTestForm.SECOND_LIST_BOX_CLASS_ID), secondClassId);
  }
}
