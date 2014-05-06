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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

public class StyledTextFieldUndoRedoSupport {
  private static final int MAX_STACK_SIZE = 25;

  private StyledText m_styledText;

  private volatile int m_inactiveCounter = 0;
  private volatile boolean m_clearRedo = true;
  private Stack<P_TextChange> m_undoStack;
  private Stack<P_TextChange> m_redoStack;

  private boolean disposed = true;

  public StyledTextFieldUndoRedoSupport(StyledText styledText) {
    m_styledText = styledText;
    init();
  }

  private void init() {
    m_undoStack = new Stack<P_TextChange>();
    m_redoStack = new Stack<P_TextChange>();

    m_styledText.addExtendedModifyListener(new ExtendedModifyListener() {
      @Override
      public void modifyText(ExtendedModifyEvent event) {
        if (isActive()) {
          if (m_clearRedo) {
            m_redoStack.clear();
          }
          if (m_undoStack.size() == MAX_STACK_SIZE) {
            m_undoStack.remove(0);
          }
          m_undoStack.push(new P_TextChange(event.start, event.length, event.replacedText));
        }
        else {
          if (m_redoStack.size() == MAX_STACK_SIZE) {
            m_redoStack.remove(0);
          }
          m_redoStack.push(new P_TextChange(event.start, event.length, event.replacedText));
        }
      }
    });

    m_styledText.addKeyListener(new KeyAdapter() {
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
    });
  }

  public StyledText getStyledText() {
    return m_styledText;
  }

  public boolean isActive() {
    return m_inactiveCounter == 0;
  }

  /**
   * set inactive to avoid changes are recorded.
   * <strong>Should be called in a try finally block only!</strong>
   * 
   * @param active
   */
  public boolean setActive(boolean active) {
    if (active) {
      m_inactiveCounter++;
    }
    else if (m_inactiveCounter > 0) {
      m_inactiveCounter--;
    }
    return m_inactiveCounter == 0;
  }

  public boolean hasUndoChanges() {
    return !m_undoStack.isEmpty();
  }

  public boolean hasRedoChanges() {
    return !m_redoStack.isEmpty();
  }

  public void undo() {
    if (!m_undoStack.isEmpty()) {
      // Get the last change
      P_TextChange change = m_undoStack.pop();

      try {
        setActive(false);
        // Replace the changed text
        m_styledText.replaceTextRange(change.getStart(), change.getLength(), change.getReplacedText());

        // Move the caret
        m_styledText.setCaretOffset(change.getStart());

        // Scroll the screen
        m_styledText.setTopIndex(m_styledText.getLineAtOffset(change.getStart()));

        // select inserted text
        int length = change.getLength() == 0 ? change.getReplacedText().length() : change.getLength();
        m_styledText.setSelectionRange(change.getStart(), length);
      }
      finally {
        setActive(true);
      }
    }
  }

  public void redo() {
    if (m_redoStack.size() > 0) {
      P_TextChange change = m_redoStack.pop();

      try {
        m_clearRedo = false;
        // Replace the changed text
        m_styledText.replaceTextRange(change.getStart(), change.getLength(), change.getReplacedText());

        // Move the caret
        m_styledText.setCaretOffset(change.getStart());

        // Scroll the screen
        m_styledText.setTopIndex(m_styledText.getLineAtOffset(change.getStart()));

        // select inserted text
        int length = change.getLength() == 0 ? change.getReplacedText().length() : change.getLength();
        m_styledText.setSelectionRange(change.getStart(), length);
      }
      finally {
        m_clearRedo = true;
      }
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
