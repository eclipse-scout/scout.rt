/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.dnd.clipboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.swing.JTextField;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.dnd.TextTransferObject;
import org.eclipse.scout.rt.ui.swing.services.common.clipboard.SwingScoutClipboardService;
import org.junit.Test;

/**
 * This JUnit test tests the clipboard functionality for Swing
 * 
 * @since 3.10.0-M2
 */
public class SwingScoutClipboardTest {

  private static final String clipboardText = "some Text";

  @Test
  public void testBestTextFlavor() throws Exception {
    setTextToClipboard();

    Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
    DataFlavor[] flavors = transferable.getTransferDataFlavors();
    assertTrue(flavors.length > 0);
    DataFlavor bestTextFlavor = new DataFlavor().selectBestTextFlavor(flavors);
    Object content = transferable.getTransferData(bestTextFlavor);
    if (content instanceof String) {
      assertEquals(clipboardText, content);
    }
  }

  @Test
  public void testMultiplePastes() throws Exception {
    setTextToClipboard();

    Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
    DataFlavor[] flavors = transferable.getTransferDataFlavors();
    assertTrue(flavors.length > 0);

    //simulates paste attempt 1
    for (DataFlavor flavor : flavors) {
      Object content = transferable.getTransferData(flavor);
      if (content instanceof String) {
        assertEquals(clipboardText, content);
      }
      else if (content instanceof InputStream) {
        assertEquals(clipboardText, getStringOfInputStream((InputStream) content, flavor.getParameter("charset")));
      }
    }

    //simulates paste attempt 2
    for (DataFlavor flavor : flavors) {
      Object content = transferable.getTransferData(flavor);
      if (content instanceof String) {
        assertEquals(clipboardText, content);
      }
      else if (content instanceof InputStream) {
        assertEquals(clipboardText, getStringOfInputStream((InputStream) content, flavor.getParameter("charset")));
      }
    }
  }

  @Test
  public void testPasteWithTextField() throws Exception {
    setTextToClipboard();

    JTextField textfield = new JTextField();
    textfield.paste();

    assertEquals(clipboardText, textfield.getText());

    textfield.paste();
    textfield.paste();
    textfield.paste();

    assertEquals(clipboardText + clipboardText + clipboardText + clipboardText, textfield.getText());
  }

  private void setTextToClipboard() throws Exception {
    SwingScoutClipboardService service = new SwingScoutClipboardService();
    TextTransferObject textTransferObject = new TextTransferObject(clipboardText);
    service.setContents(textTransferObject);
    Thread.sleep(500); //Wait half a second to make sure the content is in the clipboard
  }

  private String getStringOfInputStream(InputStream is, String encoding) throws Exception {
    BufferedReader br = null;
    Reader reader = null;
    StringBuilder sb = new StringBuilder();
    if (StringUtility.hasText(encoding)) {
      reader = new InputStreamReader(is, encoding);
    }
    else {
      reader = new InputStreamReader(is);
    }
    String line;
    br = new BufferedReader(reader);
    while ((line = br.readLine()) != null) {
      sb.append(line);
    }
    return sb.toString();
  }
}
