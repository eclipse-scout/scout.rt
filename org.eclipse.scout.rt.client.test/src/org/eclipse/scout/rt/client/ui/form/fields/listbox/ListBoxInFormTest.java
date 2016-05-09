package org.eclipse.scout.rt.client.ui.form.fields.listbox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 5.2
 */
@RunWith(ScoutClientTestRunner.class)
public class ListBoxInFormTest {

  private static final String LIST_BOX_FILTER_BOX_CLASS_ID = "a2e982d1-ea01-4d11-8655-d10c9935d8b9";
  private static final String LIST_BOX_ACTIVE_STATE_BOX_CLASS_ID = "2c4e4cf0-7bcf-46f1-a00f-19ecd2719fff";
  private static final String LIST_BOX_CHECKED_STATE_BOX_CLASS_ID = "e62c300f-f49d-4318-95ce-44a60558cfbf";

  private ListBoxTestForm m_form;

  @Before
  public void before() throws Exception {
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
