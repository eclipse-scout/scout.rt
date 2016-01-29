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
package org.eclipse.scout.rt.client.ui.messagebox;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
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
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class MessageBoxTest {

  private IDesktop m_desktopSpy;

  @Before
  public void setUp() {
    IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
    m_desktopSpy = Mockito.spy(desktop);
    TestEnvironmentClientSession.get().replaceDesktop(m_desktopSpy);
  }

  @After
  public void tearDown() {
    m_desktopSpy = null;
    TestEnvironmentClientSession.get().replaceDesktop(null);
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with a single object.
   *
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageOneObject() throws Exception {
    MessageBoxes.showDeleteConfirmationMessage("Alice");
    assertMessageBox(TEXTS.get("DeleteConfirmationText"), "- Alice\n");
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with a single object in a list. Bug 440433.
   *
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageSingletonList() throws Exception {
    MessageBoxes.showDeleteConfirmationMessage(Collections.singletonList("Alice"));
    assertMessageBox(TEXTS.get("DeleteConfirmationText"), "- Alice\n");
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with a null parameter.
   *
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageNull() throws Exception {
    MessageBoxes.showDeleteConfirmationMessage(null);
    assertMessageBox(TEXTS.get("DeleteConfirmationTextNoItemList"), null);
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with a null object as parameter.
   *
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageNullObject() throws Exception {
    String s = null;
    MessageBoxes.showDeleteConfirmationMessage("Items", s);
    assertMessageBox(TEXTS.get("DeleteConfirmationTextNoItemListX", "Items"), null);
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with an empty array.
   *
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageEmptyArray() throws Exception {
    MessageBoxes.showDeleteConfirmationMessage(new String[]{});
    assertMessageBox(TEXTS.get("DeleteConfirmationTextNoItemList", "Company"), null);
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with an empty list. Bug 440433.
   *
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageEmptyList() throws Exception {
    MessageBoxes.showDeleteConfirmationMessage(Collections.emptyList());
    assertMessageBox(TEXTS.get("DeleteConfirmationTextNoItemList", "Company"), null);
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with an empty array.
   *
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageEmptyArrayTextX() throws Exception {
    MessageBoxes.showDeleteConfirmationMessage("Company", new String[]{});
    assertMessageBox(TEXTS.get("DeleteConfirmationTextNoItemListX", "Company"), null);
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with an empty list. Bug 440433.
   *
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageEmptyListTextX() throws Exception {
    MessageBoxes.showDeleteConfirmationMessage("Company", Collections.emptyList());
    assertMessageBox(TEXTS.get("DeleteConfirmationTextNoItemListX", "Company"), null);
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with an simple array.
   *
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageArray() throws Exception {
    MessageBoxes.showDeleteConfirmationMessage(new String[]{"Alice", "Bob", "Cleo"});
    assertMessageBox(TEXTS.get("DeleteConfirmationText"), "- Alice\n- Bob\n- Cleo\n");
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with an simple list. Bug 440433.
   *
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageList() throws Exception {
    MessageBoxes.showDeleteConfirmationMessage(Arrays.asList("Alice", "Bob", "Cleo"));
    assertMessageBox(TEXTS.get("DeleteConfirmationText"), "- Alice\n- Bob\n- Cleo\n");
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with an simple list. Bug 440433.
   *
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageListAsObject() throws Exception {
    Object o = Arrays.asList("Alice", "Bob", "Cleo");
    MessageBoxes.showDeleteConfirmationMessage(o);
    assertMessageBox(TEXTS.get("DeleteConfirmationText"), "- Alice\n- Bob\n- Cleo\n");
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with an array containing a null.
   *
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageArrayTextX() throws Exception {
    MessageBoxes.showDeleteConfirmationMessage("Person", new String[]{"Alice", null, "Cleo"});
    assertMessageBox(TEXTS.get("DeleteConfirmationTextX", "Person"), "- Alice\n- \n- Cleo\n");
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with a list containing a null. Bug 440433.
   *
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageListTextX() throws Exception {
    MessageBoxes.showDeleteConfirmationMessage("Person", Arrays.asList("Alice", null, "Cleo"));
    assertMessageBox(TEXTS.get("DeleteConfirmationTextX", "Person"), "- Alice\n- \n- Cleo\n");
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with a big array.
   *
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageBigArray() throws Exception {
    MessageBoxes.showDeleteConfirmationMessage(new String[]{"Alice", "Bob", "Cleo", "Dominic", "Emma", "Fiona", "George", "Heidi", "Ingrid", "James", "Kyla", "Louis"});
    assertMessageBox(TEXTS.get("DeleteConfirmationText"), "- Alice\n- Bob\n- Cleo\n- Dominic\n- Emma\n- Fiona\n- George\n- Heidi\n- Ingrid\n- James\n  ...\n- Louis\n");
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with a big list. Bug 440433.
   *
   * @throws Exception
   */
  @Test
  public void testShowDeleteConfirmationMessageBigList() throws Exception {
    MessageBoxes.showDeleteConfirmationMessage("Numbers", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    assertMessageBox(TEXTS.get("DeleteConfirmationTextX", "Numbers"), "- 1\n- 2\n- 3\n- 4\n- 5\n- 6\n- 7\n- 8\n- 9\n- 10\n");
  }

  @Test
  public void testYesNoMessageBox() throws Exception {
    MessageBoxes.createYesNo().withHeader("header").withBody("body").show();
    assertMessageBox("header", "body");
  }

  @Test
  public void testYesNoMessageBoxWithHtml() throws Exception {
    MessageBoxes.createYesNo().withHeader("header").withBody("body").withHtml(HTML.raw("<h1>title</h1>")).show();
    assertMessageBox("header", "body", "<h1>title</h1>");
  }

  @Test
  public void testYesNoMessageBoxWithEncodedHtml() throws Exception {
    MessageBoxes.createYesNo().withHeader("header").withBody("body").withHtml(HTML.h1("title & sub-title")).show();
    assertMessageBox("header", "body", "<h1>title &amp; sub-title</h1>");
  }

  private void assertMessageBox(String expectedIntro, String expectedAction) {
    assertMessageBox(expectedIntro, expectedAction, null);
  }

  private void assertMessageBox(String expectedIntro, String expectedAction, String expectedHtml) {
    ArgumentCaptor<MessageBox> argument = ArgumentCaptor.forClass(MessageBox.class);
    Mockito.verify(m_desktopSpy).showMessageBox(argument.capture());

    MessageBox messageBox = argument.getValue();
    assertEquals("Intro text", expectedIntro, messageBox.getHeader());
    assertEquals("Action text", expectedAction, messageBox.getBody());
    assertEquals("Html", expectedHtml, messageBox.getHtml() == null ? null : messageBox.getHtml().toEncodedHtml());
    assertEquals("Yes button text", TEXTS.get("YesButton"), messageBox.getYesButtonText());
    assertEquals("No button text", TEXTS.get("NoButton"), messageBox.getNoButtonText());
    assertEquals("Cancel button text", null, messageBox.getCancelButtonText());
  }

}
