/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.messagebox;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.platform.text.TEXTS;
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
   */
  @Test
  public void testShowDeleteConfirmationMessageOneObject() {
    MessageBoxes.showDeleteConfirmationMessage("Alice");
    assertMessageBox(TEXTS.get("DeleteConfirmationText"), null, "<ul><li>Alice</li></ul>");
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with a single object in a list. Bug 440433.
   */
  @Test
  public void testShowDeleteConfirmationMessageSingletonList() {
    MessageBoxes.showDeleteConfirmationMessage(Collections.singletonList("Alice"));
    assertMessageBox(TEXTS.get("DeleteConfirmationText"), null, "<ul><li>Alice</li></ul>");
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with a null parameter.
   */
  @Test
  public void testShowDeleteConfirmationMessageNull() {
    MessageBoxes.showDeleteConfirmationMessage(null);
    assertMessageBox(TEXTS.get("DeleteConfirmationTextNoItemList"), null);
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with a null object as parameter.
   */
  @Test
  public void testShowDeleteConfirmationMessageNullObject() {
    String s = null;
    MessageBoxes.showDeleteConfirmationMessage("Items", s);
    assertMessageBox(TEXTS.get("DeleteConfirmationTextNoItemListX", "Items"), null);
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with an empty array.
   */
  @Test
  public void testShowDeleteConfirmationMessageEmptyArray() {
    MessageBoxes.showDeleteConfirmationMessage(new String[]{});
    assertMessageBox(TEXTS.get("DeleteConfirmationTextNoItemList", "Company"), null);
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with an empty list. Bug 440433.
   */
  @Test
  public void testShowDeleteConfirmationMessageEmptyList() {
    MessageBoxes.showDeleteConfirmationMessage(Collections.emptyList());
    assertMessageBox(TEXTS.get("DeleteConfirmationTextNoItemList", "Company"), null);
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with an empty array.
   */
  @Test
  public void testShowDeleteConfirmationMessageEmptyArrayTextX() {
    MessageBoxes.showDeleteConfirmationMessage("Company", new String[]{});
    assertMessageBox(TEXTS.get("DeleteConfirmationTextNoItemListX", "Company"), null);
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with an empty list. Bug 440433.
   */
  @Test
  public void testShowDeleteConfirmationMessageEmptyListTextX() {
    MessageBoxes.showDeleteConfirmationMessage("Company", Collections.emptyList());
    assertMessageBox(TEXTS.get("DeleteConfirmationTextNoItemListX", "Company"), null);
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with an simple array.
   */
  @Test
  public void testShowDeleteConfirmationMessageArray() {
    MessageBoxes.showDeleteConfirmationMessage(new String[]{"Alice", "Bob", "Cleo"});
    assertMessageBox(TEXTS.get("DeleteConfirmationText"), null, "<ul><li>Alice</li><li>Bob</li><li>Cleo</li></ul>");
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with a simple list. Bug 440433.
   */
  @Test
  public void testShowDeleteConfirmationMessageList() {
    MessageBoxes.showDeleteConfirmationMessage(Arrays.asList("Alice", "Bob", "Cleo"));
    assertMessageBox(TEXTS.get("DeleteConfirmationText"), null, "<ul><li>Alice</li><li>Bob</li><li>Cleo</li></ul>");
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with a simple list. Bug 440433.
   */
  @Test
  public void testShowDeleteConfirmationMessageListAsObject() {
    Object o = Arrays.asList("Alice", "Bob", "Cleo");
    MessageBoxes.showDeleteConfirmationMessage(o);
    assertMessageBox(TEXTS.get("DeleteConfirmationText"), null, "<ul><li>Alice</li><li>Bob</li><li>Cleo</li></ul>");
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with an array containing a null.
   */
  @Test
  public void testShowDeleteConfirmationMessageArrayTextX() {
    MessageBoxes.showDeleteConfirmationMessage("Person", new String[]{"Alice", null, "Cleo"});
    assertMessageBox(TEXTS.get("DeleteConfirmationTextX", "Person"), null, "<ul><li>Alice</li><li></li><li>Cleo</li></ul>");
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with a list containing a null. Bug 440433.
   */
  @Test
  public void testShowDeleteConfirmationMessageListTextX() {
    MessageBoxes.showDeleteConfirmationMessage("Person", Arrays.asList("Alice", null, "Cleo"));
    assertMessageBox(TEXTS.get("DeleteConfirmationTextX", "Person"), null, "<ul><li>Alice</li><li></li><li>Cleo</li></ul>");
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with a big array.
   */
  @Test
  public void testShowDeleteConfirmationMessageBigArray() {
    MessageBoxes.showDeleteConfirmationMessage(new String[]{"Alice", "Bob", "Cleo", "Dominic", "Emma", "Fiona", "George", "Heidi", "Ingrid", "James", "Kyla", "Louis", "Maria"});
    assertMessageBox(TEXTS.get("DeleteConfirmationText"), null,
        "<ul><li>Alice</li><li>Bob</li><li>Cleo</li><li>Dominic</li><li>Emma</li><li>Fiona</li><li>George</li><li>Heidi</li><li>Ingrid</li><li>James</li><li>...  </li><li>3 more</li></ul>");
  }

  /**
   * Test method for {@link MessageBoxes#showDeleteConfirmationMessage} with a big list. Bug 440433.
   */
  @Test
  public void testShowDeleteConfirmationMessageBigList() {
    MessageBoxes.showDeleteConfirmationMessage("Numbers", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
    assertMessageBox(TEXTS.get("DeleteConfirmationTextX", "Numbers"), null, "<ul><li>1</li><li>2</li><li>3</li><li>4</li><li>5</li><li>6</li><li>7</li><li>8</li><li>9</li><li>10</li><li>11</li><li>12</li></ul>");
  }

  @Test
  public void testYesNoMessageBox() {
    MessageBoxes.createYesNo().withHeader("header").withBody("body").show();
    assertMessageBox("header", "body");
  }

  @Test
  public void testYesNoMessageBoxWithHtml() {
    MessageBoxes.createYesNo().withHeader("header").withBody("body").withHtml(HTML.raw("<h1>title</h1>")).show();
    assertMessageBox("header", "body", "<h1>title</h1>");
  }

  @Test
  public void testYesNoMessageBoxWithEncodedHtml() {
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
    assertEquals("Html", expectedHtml, messageBox.getHtml() == null ? null : messageBox.getHtml().toHtml());
    assertEquals("Yes button text", TEXTS.get("YesButton"), messageBox.getYesButtonText());
    assertEquals("No button text", TEXTS.get("NoButton"), messageBox.getNoButtonText());
    assertEquals("Cancel button text", null, messageBox.getCancelButtonText());
  }
}
