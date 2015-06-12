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
    String header = "Intro Text";
    String body = "Action Text";

    SwingScoutMessageBox box = new P_SwingScoutMessageBox(null, header, body);
    box.initializeSwing();

    JDialog dialog = box.getSwingDialog();
    assertEquals(header, dialog.getTitle()); // title doesn't exist anymore since N release, header is used instead in Swing

    JLabel introLabel = findIntroLabel(dialog);
    assertEquals(header, introLabel.getText());

    JLabel actionLabel = findActionLabel(dialog);
    assertEquals(body, actionLabel.getText());
  }

  /**
   * In case the header or body is too long, surround the text with html tags to have text wrapping
   */
  @Test
  public void testLongWrap() {
    String header = "Intro: Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";
    String body = "Content: Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";

    SwingScoutMessageBox box = new P_SwingScoutMessageBox(null, header, body);
    box.initializeSwing();

    JDialog dialog = box.getSwingDialog();
    assertEquals(header, dialog.getTitle()); // title doesn't exist anymore since N release, header is used instead in Swing

    JLabel introLabel = findIntroLabel(dialog);
    assertEquals("<html>" + header + "</html>", introLabel.getText());

    JLabel actionLabel = findActionLabel(dialog);
    assertEquals("<html>" + body + "</html>", actionLabel.getText());
  }

  /**
   * If the header or body is too long and has additional linebreaks, they must be converted to html br tags
   */
  @Test
  public void testLongWrapWithLinebreakes() {
    String header = "Intro:\n\nLorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";
    String body = "Content:\n\nLorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";

    SwingScoutMessageBox box = new P_SwingScoutMessageBox(null, header, body);
    box.initializeSwing();

    JDialog dialog = box.getSwingDialog();
    assertEquals(header, dialog.getTitle()); // title doesn't exist anymore since N release, header is used instead in Swing

    JLabel introLabel = findIntroLabel(dialog);
    assertEquals("<html>" + StringUtility.replaceNewLines(header, "<br/>") + "</html>", introLabel.getText());

    JLabel actionLabel = findActionLabel(dialog);
    assertEquals("<html>" + StringUtility.replaceNewLines(body, "<br/>") + "</html>", actionLabel.getText());
  }

  /**
   * Find the header label in the SwingScoutMessageBox
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
   * Find the body label in the SwingScoutMessageBox
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

    private String m_header;
    private String m_body;

    public P_SwingScoutMessageBox(Window swingParent, String header, String body) {
      super(swingParent);
      m_header = header;
      m_body = body;
    }

    @Override
    public IMessageBox getScoutMessageBox() {
      IMessageBox scoutObject = mock(IMessageBox.class);
      when(scoutObject.header()).thenReturn(m_header);
      when(scoutObject.body()).thenReturn(m_body);
      return scoutObject;
    }
  }
}
