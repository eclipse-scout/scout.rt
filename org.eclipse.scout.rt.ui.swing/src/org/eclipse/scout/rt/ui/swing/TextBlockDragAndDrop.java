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
package org.eclipse.scout.rt.ui.swing;

import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;

import javax.swing.text.JTextComponent;

public final class TextBlockDragAndDrop {
  private static final boolean DND_ENABLED = false;// disabled: dnd would prevent the functionality of selecting text in textarea

  public static void attach(JTextComponent c) {
    if (DND_ENABLED) {
      new P_DragSource(c);
    }
  }

  private TextBlockDragAndDrop() {
  }

  /**
   * Implementation of DropSource's DragGestureListener
   */
  private static class P_DragSource implements DragGestureListener, DragSourceListener {
    public P_DragSource(JTextComponent c) {
      DragSource dragSource = DragSource.getDefaultDragSource();
      // create the recognizer
      dragSource.createDefaultDragGestureRecognizer(c, DnDConstants.ACTION_COPY_OR_MOVE, this);
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent e) {
      JTextComponent textComp = (JTextComponent) e.getComponent();
      if (!textComp.isEnabled()) {
        String selText = null;
        int selStart = textComp.getSelectionStart();
        int selEnd = textComp.getSelectionStart();
        int pos = textComp.viewToModel(e.getDragOrigin());
        if (selEnd - selStart > 0) {
          if (pos >= selStart && pos <= selEnd) {
            selText = textComp.getSelectedText();
          }
        }
        else {
          selText = textComp.getText();
        }
        if (selText != null && selText.length() > 0) {
          e.startDrag(DragSource.DefaultCopyDrop, new StringSelection(selText), this);
        }
      }
    }

    @Override
    public void dragDropEnd(DragSourceDropEvent e) {
    }

    @Override
    public void dragEnter(DragSourceDragEvent e) {
    }

    @Override
    public void dragExit(DragSourceEvent e) {
    }

    @Override
    public void dragOver(DragSourceDragEvent e) {
    }

    @Override
    public void dropActionChanged(DragSourceDragEvent e) {
    }
  }// end class

}
