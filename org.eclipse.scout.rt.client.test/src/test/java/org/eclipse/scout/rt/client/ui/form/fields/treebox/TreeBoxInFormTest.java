package org.eclipse.scout.rt.client.ui.form.fields.treebox;

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
public class TreeBoxInFormTest {

  private static final String TREE_BOX_FILTER_BOX_CLASS_ID = "5cfd2944-5dfd-4b66-ae45-419bb1b71378";
  private static final String TREE_BOX_ACTIVE_STATE_BOX_CLASS_ID = "36299662-6bd3-47e2-b3bc-9d54be265de4";
  private static final String TREE_BOX_CHECKED_STATE_BOX_CLASS_ID = "a292b6e6-0e77-4852-a346-dc27f72b9f57";

  private TreeBoxTestForm m_form;

  @Before
  public void before() {
    m_form = new TreeBoxTestForm();
  }

  @Test
  public void testTreeBoxClassIds() {
    assertEquals(TreeBoxTestForm.FIRST_TREE_BOX_CLASS_ID, m_form.getFirstTreeBox().classId());
    assertEquals(TreeBoxTestForm.SECOND_TREE_BOX_CLASS_ID, m_form.getSecondTreeBox().classId());
  }

  @Test
  public void testTreeBoxFilterBoxClassIds() {
    String firstClassId = m_form.getFirstTreeBox().getTreeBoxFilterBox().classId();
    String secondClassId = m_form.getSecondTreeBox().getTreeBoxFilterBox().classId();
    assertNotEquals(firstClassId, secondClassId);

    assertEquals(TREE_BOX_FILTER_BOX_CLASS_ID + ITypeWithClassId.ID_CONCAT_SYMBOL + TreeBoxTestForm.FIRST_TREE_BOX_CLASS_ID, firstClassId);
    assertEquals(TREE_BOX_FILTER_BOX_CLASS_ID + ITypeWithClassId.ID_CONCAT_SYMBOL + TreeBoxTestForm.SECOND_TREE_BOX_CLASS_ID, secondClassId);
  }

  @Test
  public void testTreeBoxFilterBoxActiveStateRadioButtonGroupClassIds() {
    String firstClassId = m_form.getFirstTreeBox().getTreeBoxFilterBox().getActiveStateRadioButtonGroup().classId();
    String secondClassId = m_form.getSecondTreeBox().getTreeBoxFilterBox().getActiveStateRadioButtonGroup().classId();
    assertNotEquals(firstClassId, secondClassId);

    assertEquals(StringUtility.join(ITypeWithClassId.ID_CONCAT_SYMBOL,
        TREE_BOX_ACTIVE_STATE_BOX_CLASS_ID,
        TREE_BOX_FILTER_BOX_CLASS_ID,
        TreeBoxTestForm.FIRST_TREE_BOX_CLASS_ID), firstClassId);

    assertEquals(StringUtility.join(ITypeWithClassId.ID_CONCAT_SYMBOL,
        TREE_BOX_ACTIVE_STATE_BOX_CLASS_ID,
        TREE_BOX_FILTER_BOX_CLASS_ID,
        TreeBoxTestForm.SECOND_TREE_BOX_CLASS_ID), secondClassId);
  }

  @Test
  public void testTreeBoxFilterBoxCheckedStateRadioButtonGroupClassIds() {
    String firstClassId = m_form.getFirstTreeBox().getTreeBoxFilterBox().getCheckedStateRadioButtonGroup().classId();
    String secondClassId = m_form.getSecondTreeBox().getTreeBoxFilterBox().getCheckedStateRadioButtonGroup().classId();
    assertNotEquals(firstClassId, secondClassId);

    assertEquals(StringUtility.join(ITypeWithClassId.ID_CONCAT_SYMBOL,
        TREE_BOX_CHECKED_STATE_BOX_CLASS_ID,
        TREE_BOX_FILTER_BOX_CLASS_ID,
        TreeBoxTestForm.FIRST_TREE_BOX_CLASS_ID), firstClassId);

    assertEquals(StringUtility.join(ITypeWithClassId.ID_CONCAT_SYMBOL,
        TREE_BOX_CHECKED_STATE_BOX_CLASS_ID,
        TREE_BOX_FILTER_BOX_CLASS_ID,
        TreeBoxTestForm.SECOND_TREE_BOX_CLASS_ID), secondClassId);
  }
}
