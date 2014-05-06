/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.action.menu.text;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 *
 */
public class TextAccess implements ITextAccess {

  private Text m_textControl;

  public TextAccess(Text textControl) {
    m_textControl = textControl;
  }

  @Override
  public boolean isEnabled() {
    return m_textControl.isEnabled();
  }

  @Override
  public boolean isEditable() {
    return m_textControl.getEditable();
  }

  @Override
  public boolean hasSelection() {
    return m_textControl.getSelectionCount() > 0;
  }

  @Override
  public Point getSelection() {
    return m_textControl.getSelection();
  }

  @Override
  public String getText() {
    return m_textControl.getText();
  }

  @Override
  public String getSelectedText() {
    return m_textControl.getSelectionText();
  }

  @Override
  public Control getTextControl() {
    return m_textControl;
  }

  @Override
  public boolean isMasked() {
    return (m_textControl.getStyle() & SWT.PASSWORD) != 0;
  }

  @Override
  public void copy() {
    m_textControl.copy();
  }

  @Override
  public boolean hasTextOnClipboard() {
    TextTransfer plainTextTransfer = TextTransfer.getInstance();
    Clipboard clipboard = null;
    try {
      clipboard = new Clipboard(m_textControl.getDisplay());
      String contents = (String) clipboard.getContents(plainTextTransfer, DND.CLIPBOARD);
      return contents != null && contents.length() > 0;
    }
    finally {
      if (clipboard != null) {
        clipboard.dispose();
      }
    }
  }

  @Override
  public void paste() {
    m_textControl.paste();
  }

  @Override
  public void cut() {
    m_textControl.cut();
  }
}
