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
package org.eclipse.scout.rt.ui.swing.window.messagebox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.junit.Test;

/**
 * Tests for {@link SwingScoutMessageBox}
 *
 * @since 4.0.0-M7
 */
public class SwingScoutMessageBoxUiTest {

  @Test
  public void testLabels() {
    String title = "Title";
    String introText = "Intro Text";
    String actionText = "Action Text";

    SwingScoutMessageBox box = new P_SwingScoutMessageBox(null, title, introText, actionText);
    box.initializeSwing();

    JDialog dialog = box.getSwingDialog();
    assertEquals(title, dialog.getTitle());

    JLabel introLabel = findIntroLabel(dialog);
    assertEquals(introText, introLabel.getText());

    JLabel actionLabel = findActionLabel(dialog);
    assertEquals(actionText, actionLabel.getText());
  }

  /**
   * In case the introText or actionText is too long, surround the text with html tags to have text wrapping
   */
  @Test
  public void testLongWrap() {
    String title = "Title more text";
    String introText = "Intro: Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";
    String actionText = "Content: Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";

    SwingScoutMessageBox box = new P_SwingScoutMessageBox(null, title, introText, actionText);
    box.initializeSwing();

    JDialog dialog = box.getSwingDialog();
    assertEquals(title, dialog.getTitle());

    JLabel introLabel = findIntroLabel(dialog);
    assertEquals("<html>" + introText + "</html>", introLabel.getText());

    JLabel actionLabel = findActionLabel(dialog);
    assertEquals("<html>" + actionText + "</html>", actionLabel.getText());
  }

  /**
   * If the introText or actionText is too long and has additional linebreaks, they must be converted to html br tags
   */
  @Test
  public void testLongWrapWithLinebreakes() {
    String title = "Title even more text";
    String introText = "Intro:\n\nLorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";
    String actionText = "Content:\n\nLorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";

    SwingScoutMessageBox box = new P_SwingScoutMessageBox(null, title, introText, actionText);
    box.initializeSwing();

    JDialog dialog = box.getSwingDialog();
    assertEquals(title, dialog.getTitle());

    JLabel introLabel = findIntroLabel(dialog);
    assertEquals("<html>" + StringUtility.replaceNewLines(introText, "<br/>") + "</html>", introLabel.getText());

    JLabel actionLabel = findActionLabel(dialog);
    assertEquals("<html>" + StringUtility.replaceNewLines(actionText, "<br/>") + "</html>", actionLabel.getText());
  }

  /**
   * Find the intro label in the SwingScoutMessageBox
   */
  private JLabel findIntroLabel(JDialog dialog) {
    JPanel contentPane = (JPanel) dialog.getContentPane();
    assertNotNull(contentPane);
    assertTrue(contentPane.getComponentCount() > 0);
    JPanel introPanel = (JPanel) contentPane.getComponent(0);
    JLabel introPanelLabel = (JLabel) introPanel.getComponent(0);
    return introPanelLabel;
  }

  /**
   * Find the action label in the SwingScoutMessageBox
   */
  private JLabel findActionLabel(JDialog dialog) {
    JPanel contentPane = (JPanel) dialog.getContentPane();
    assertNotNull(contentPane);
    assertTrue(contentPane.getComponentCount() > 1);
    JPanel actionPanel = (JPanel) contentPane.getComponent(1);
    JLabel actionPanelLabel = (JLabel) actionPanel.getComponent(0);
    return actionPanelLabel;
  }

  private class P_SwingScoutMessageBox extends SwingScoutMessageBox {

    private String m_title;
    private String m_introText;
    private String m_actionText;

    public P_SwingScoutMessageBox(Window swingParent, String title, String introText, String actionText) {
      super(swingParent);
      m_title = title;
      m_introText = introText;
      m_actionText = actionText;
    }

    @Override
    public IMessageBox getScoutMessageBox() {
      IMessageBox scoutObject = mock(IMessageBox.class);
      when(scoutObject.getTitle()).thenReturn(m_title);
      when(scoutObject.getIntroText()).thenReturn(m_introText);
      when(scoutObject.getActionText()).thenReturn(m_actionText);
      return scoutObject;
    }

  }
}
