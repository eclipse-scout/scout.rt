/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.messagebox;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * Tests for {@link MessageBox}.
 * 
 * @since Scout 4.0.1
 */
@RunWith(ScoutClientTestRunner.class)
public class MessageBoxTest {

  private IDesktop m_desktopSpy;

  @Before
  public void setUp() {
    IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
    m_desktopSpy = Mockito.spy(desktop);
    TestEnvironmentClientSession.get().replaceDesktop(m_desktopSpy);
  }

  @After
  public void tearDown() {
    m_desktopSpy = null;
    TestEnvironmentClientSession.get().replaceDesktop(null);
  }

  /**
   * Test method for {@link MessageBox#showDeleteConfirmationMessage} with a single object.
   * 
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageOneObject() throws Exception {
    MessageBox.showDeleteConfirmationMessage("Alice");
    assertMessageBox(TEXTS.get("DeleteConfirmationText"), "- Alice\n");
  }

  /**
   * Test method for {@link MessageBox#showDeleteConfirmationMessage} with a single object in a list.
   * Bug 440433.
   * 
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageSingletonList() throws Exception {
    MessageBox.showDeleteConfirmationMessage(Collections.singletonList("Alice"));
    assertMessageBox(TEXTS.get("DeleteConfirmationText"), "- Alice\n");
  }

  /**
   * Test method for {@link MessageBox#showDeleteConfirmationMessage} with a null parameter.
   * 
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageNull() throws Exception {
    MessageBox.showDeleteConfirmationMessage(null);
    assertMessageBox(TEXTS.get("DeleteConfirmationTextNoItemList"), null);
  }

  /**
   * Test method for {@link MessageBox#showDeleteConfirmationMessage} with a null object as parameter.
   * 
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageNullObject() throws Exception {
    String s = null;
    MessageBox.showDeleteConfirmationMessage("Items", s);
    assertMessageBox(TEXTS.get("DeleteConfirmationTextNoItemListX", "Items"), null);
  }

  /**
   * Test method for {@link MessageBox#showDeleteConfirmationMessage} with an empty array.
   * 
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageEmptyArray() throws Exception {
    MessageBox.showDeleteConfirmationMessage(new String[]{});
    assertMessageBox(TEXTS.get("DeleteConfirmationTextNoItemList", "Company"), null);
  }

  /**
   * Test method for {@link MessageBox#showDeleteConfirmationMessage} with an empty list.
   * Bug 440433.
   * 
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageEmptyList() throws Exception {
    MessageBox.showDeleteConfirmationMessage(Collections.emptyList());
    assertMessageBox(TEXTS.get("DeleteConfirmationTextNoItemList", "Company"), null);
  }

  /**
   * Test method for {@link MessageBox#showDeleteConfirmationMessage} with an empty array.
   * 
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageEmptyArrayTextX() throws Exception {
    MessageBox.showDeleteConfirmationMessage("Company", new String[]{});
    assertMessageBox(TEXTS.get("DeleteConfirmationTextNoItemListX", "Company"), null);
  }

  /**
   * Test method for {@link MessageBox#showDeleteConfirmationMessage} with an empty list.
   * Bug 440433.
   * 
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageEmptyListTextX() throws Exception {
    MessageBox.showDeleteConfirmationMessage("Company", Collections.emptyList());
    assertMessageBox(TEXTS.get("DeleteConfirmationTextNoItemListX", "Company"), null);
  }

  /**
   * Test method for {@link MessageBox#showDeleteConfirmationMessage} with an simple array.
   * 
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageArray() throws Exception {
    MessageBox.showDeleteConfirmationMessage(new String[]{"Alice", "Bob", "Cleo"});
    assertMessageBox(TEXTS.get("DeleteConfirmationText"), "- Alice\n- Bob\n- Cleo\n");
  }

  /**
   * Test method for {@link MessageBox#showDeleteConfirmationMessage} with an simple list.
   * Bug 440433.
   * 
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageList() throws Exception {
    MessageBox.showDeleteConfirmationMessage(Arrays.asList("Alice", "Bob", "Cleo"));
    assertMessageBox(TEXTS.get("DeleteConfirmationText"), "- Alice\n- Bob\n- Cleo\n");
  }

  /**
   * Test method for {@link MessageBox#showDeleteConfirmationMessage} with an simple list.
   * Bug 440433.
   * 
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageListAsObject() throws Exception {
    Object o = Arrays.asList("Alice", "Bob", "Cleo");
    MessageBox.showDeleteConfirmationMessage(o);
    assertMessageBox(TEXTS.get("DeleteConfirmationText"), "- Alice\n- Bob\n- Cleo\n");
  }

  /**
   * Test method for {@link MessageBox#showDeleteConfirmationMessage} with an array containing a null.
   * 
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageArrayTextX() throws Exception {
    MessageBox.showDeleteConfirmationMessage("Person", new String[]{"Alice", null, "Cleo"});
    assertMessageBox(TEXTS.get("DeleteConfirmationTextX", "Person"), "- Alice\n- \n- Cleo\n");
  }

  /**
   * Test method for {@link MessageBox#showDeleteConfirmationMessage} with a list containing a null.
   * Bug 440433.
   * 
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageListTextX() throws Exception {
    MessageBox.showDeleteConfirmationMessage("Person", Arrays.asList("Alice", null, "Cleo"));
    assertMessageBox(TEXTS.get("DeleteConfirmationTextX", "Person"), "- Alice\n- \n- Cleo\n");
  }

  /**
   * Test method for {@link MessageBox#showDeleteConfirmationMessage} with a big array.
   * 
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageBigArray() throws Exception {
    MessageBox.showDeleteConfirmationMessage(new String[]{"Alice", "Bob", "Cleo", "Dominic", "Emma", "Fiona", "George", "Heidi", "Ingrid", "James", "Kyla", "Louis"});
    assertMessageBox(TEXTS.get("DeleteConfirmationText"), "- Alice\n- Bob\n- Cleo\n- Dominic\n- Emma\n- Fiona\n- George\n- Heidi\n- Ingrid\n- James\n  ...\n- Louis\n");
  }

  /**
   * Test method for {@link MessageBox#showDeleteConfirmationMessage} with a big list.
   * Bug 440433.
   * 
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageBigList() throws Exception {
    MessageBox.showDeleteConfirmationMessage("Numbers", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    assertMessageBox(TEXTS.get("DeleteConfirmationTextX", "Numbers"), "- 1\n- 2\n- 3\n- 4\n- 5\n- 6\n- 7\n- 8\n- 9\n- 10\n");
  }

  private void assertMessageBox(String expectedIntro, String expectedAction) {
    ArgumentCaptor<MessageBox> argument = ArgumentCaptor.forClass(MessageBox.class);
    Mockito.verify(m_desktopSpy).addMessageBox(argument.capture());

    MessageBox messageBox = argument.getValue();
    assertEquals("Title", TEXTS.get("DeleteConfirmationTitle"), messageBox.getTitle());
    assertEquals("Intro text", expectedIntro, messageBox.getIntroText());
    assertEquals("Action text", expectedAction, messageBox.getActionText());
    assertEquals("Yes button text", TEXTS.get("YesButton"), messageBox.getYesButtonText());
    assertEquals("No button text", TEXTS.get("NoButton"), messageBox.getNoButtonText());
    assertEquals("Cancel button text", null, messageBox.getCancelButtonText());
  }

}
