/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.internal;

import java.util.Stack;

import org.eclipse.scout.rt.ui.swt.ext.StyledTextEx;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MenuItem;

public class StyledTextFieldUndoRedoSupport {

  private StyledTextEx m_styledText;

  private MenuListener m_menuListener;
  private MenuItem m_undoRedoSeparator;
  private MenuItem m_undoItem;
  private MenuItem m_redoItem;
  private SelectionAdapter m_undoSelectionAdapter;
  private SelectionAdapter m_redoSelectionAdapter;

  private ExtendedModifyListener m_modifyListener;
  private KeyAdapter m_keyAdapter;

  private static final int MAX_STACK_SIZE = 25;
  private boolean fillUndo = true;
  private boolean clearRedo = true;
  private Stack<P_TextChange> undoStack;
  private Stack<P_TextChange> redoStack;

  private boolean disposed = true;

  public StyledTextFieldUndoRedoSupport(StyledTextEx styledText) {
    m_styledText = styledText;
    init();
  }

  private void init() {

    m_menuListener = new MenuListener() {
      @Override
      public void menuHidden(MenuEvent e) {
      }

      @Override
      public void menuShown(MenuEvent e) {
        if (m_styledText.isEnabled()) {
          m_undoItem.setEnabled(!undoStack.isEmpty() && m_styledText.getEditable());
          m_redoItem.setEnabled(!redoStack.isEmpty() && m_styledText.getEditable());
        }
      }
    };
    m_styledText.getMenu().addMenuListener(m_menuListener);

    undoStack = new Stack<P_TextChange>();
    redoStack = new Stack<P_TextChange>();

    m_modifyListener = new ExtendedModifyListener() {
      @Override
      public void modifyText(ExtendedModifyEvent event) {
        if (fillUndo) {
          if (clearRedo) {
            redoStack.clear();
          }
          if (undoStack.size() == MAX_STACK_SIZE) {
            undoStack.remove(0);
          }
          undoStack.push(new P_TextChange(event.start, event.length, event.replacedText));
        }
        else {
          if (redoStack.size() == MAX_STACK_SIZE) {
            redoStack.remove(0);
          }
          redoStack.push(new P_TextChange(event.start, event.length, event.replacedText));
        }
      }
    };
    m_styledText.addExtendedModifyListener(m_modifyListener);

    m_undoRedoSeparator = new MenuItem(m_styledText.getMenu(), SWT.SEPARATOR);
    m_undoItem = new MenuItem(m_styledText.getMenu(), SWT.PUSH);
    m_undoSelectionAdapter = new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        undo();
      }
    };
    m_undoItem.addSelectionListener(m_undoSelectionAdapter);
    m_undoItem.setText(SwtUtility.getNlsText(Display.getCurrent(), "Undo"));

    m_redoItem = new MenuItem(m_styledText.getMenu(), SWT.PUSH);
    m_redoSelectionAdapter = new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        redo();
      }
    };
    m_redoItem.addSelectionListener(m_redoSelectionAdapter);
    m_redoItem.setText(SwtUtility.getNlsText(Display.getCurrent(), "Redo"));

    m_keyAdapter = new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if ((e.stateMask & SWT.MODIFIER_MASK) == SWT.CTRL) {
          switch (e.keyCode) {
            case 'y':
            case 'Y':
              redo();
              break;
            case 'z':
            case 'Z':
              undo();
              break;
            default:
              //ignore everything else
          }
        }
      }
    };
    m_styledText.addKeyListener(m_keyAdapter);

    disposed = false;
  }

  public boolean isDisposed() {
    return disposed;
  }

  public void dispose() {
    if (!m_styledText.isDisposed()) {
      m_styledText.removeExtendedModifyListener(m_modifyListener);
      m_styledText.removeKeyListener(m_keyAdapter);

      if (m_styledText.getMenu() != null && !m_styledText.getMenu().isDisposed()) {
        m_styledText.getMenu().removeMenuListener(m_menuListener);
      }
    }

    if (!m_undoRedoSeparator.isDisposed()) {
      m_undoRedoSeparator.dispose();
    }

    if (!m_undoItem.isDisposed()) {
      m_undoItem.removeSelectionListener(m_undoSelectionAdapter);
      m_undoItem.dispose();
    }

    if (!m_redoItem.isDisposed()) {
      m_redoItem.removeSelectionListener(m_redoSelectionAdapter);
      m_redoItem.dispose();
    }

    undoStack.clear();
    redoStack.clear();

    disposed = true;
  }

  public void clearStacks() {
    undoStack.clear();
    redoStack.clear();
  }

  private void undo() {
    if (!undoStack.isEmpty()) {
      // Get the last change
      P_TextChange change = undoStack.pop();

      fillUndo = false;
      // Replace the changed text
      m_styledText.replaceTextRange(change.getStart(), change.getLength(), change.getReplacedText());

      // Move the caret
      m_styledText.setCaretOffset(change.getStart());

      // Scroll the screen
      m_styledText.setTopIndex(m_styledText.getLineAtOffset(change.getStart()));

      // select inserted text
      int length = change.getLength() == 0 ? change.getReplacedText().length() : change.getLength();
      m_styledText.setSelectionRange(change.getStart(), length);
      fillUndo = true;
    }
  }

  private void redo() {
    if (redoStack.size() > 0) {
      P_TextChange change = redoStack.pop();

      clearRedo = false;
      // Replace the changed text
      m_styledText.replaceTextRange(change.getStart(), change.getLength(), change.getReplacedText());

      // Move the caret
      m_styledText.setCaretOffset(change.getStart());

      // Scroll the screen
      m_styledText.setTopIndex(m_styledText.getLineAtOffset(change.getStart()));

      // select inserted text
      int length = change.getLength() == 0 ? change.getReplacedText().length() : change.getLength();
      m_styledText.setSelectionRange(change.getStart(), length);
      clearRedo = true;
    }
  }

  class P_TextChange {
    // The starting offset of the change
    private int start;

    // The length of the change
    private int length;

    // The replaced text
    String replacedText;

    /**
     * Constructs a TextChange
     * 
     * @param start
     *          the starting offset of the change
     * @param length
     *          the length of the change
     * @param replacedText
     *          the text that was replaced
     */
    public P_TextChange(int start, int length, String replacedText) {
      this.start = start;
      this.length = length;
      this.replacedText = replacedText;
    }

    /**
     * Returns the start
     * 
     * @return int
     */
    public int getStart() {
      return start;
    }

    /**
     * Returns the length
     * 
     * @return int
     */
    public int getLength() {
      return length;
    }

    /**
     * Returns the replacedText
     * 
     * @return String
     */
    public String getReplacedText() {
      return replacedText;
    }
  }
}
