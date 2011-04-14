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

import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public final class UndoableEditObserver implements FocusListener, UndoableEditListener, KeyListener {
  private UndoManager m_undoManager;

  private UndoableEditObserver() {
  }

  public static void attach(JTextComponent c) {
    new UndoableEditObserver(c);
  }

  private UndoableEditObserver(JTextComponent c) {
    if (c.getDocument() != null) {
      c.getDocument().addUndoableEditListener(this);
    }
    c.addKeyListener(this);
    c.addFocusListener(this);
    // re-attach when document changes
    c.addPropertyChangeListener("document", new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        Document old = (Document) e.getOldValue();
        Document doc = (Document) e.getNewValue();
        if (old != null) {
          old.removeUndoableEditListener(UndoableEditObserver.this);
        }
        if (doc != null) {
          doc.addUndoableEditListener(UndoableEditObserver.this);
        }
      }
    });
  }

  /**
   * Implementation of FocusListener
   */
  @Override
  public void focusGained(FocusEvent f) {
    if (!f.isTemporary()) {
      createUndoMananger();
    }
  }

  @Override
  public void focusLost(FocusEvent f) {
    if (!f.isTemporary()) {
      removeUndoMananger();
    }
  }

  /**
   * Implementation of UndoableEditListener
   */
  @Override
  public void undoableEditHappened(UndoableEditEvent e) {
    if (m_undoManager != null) {
      m_undoManager.addEdit(e.getEdit());
    }
  }

  /**
   * Implementation of KeyListener
   */
  @Override
  public void keyReleased(KeyEvent e) {
  }

  @Override
  public void keyTyped(KeyEvent e) {
  }

  @Override
  public void keyPressed(KeyEvent e) {
    // Ctrl-Z
    if ((e.getKeyCode() == KeyEvent.VK_Z) && (e.isControlDown()) && (!e.isConsumed())) {
      undoAction();
    }
    else if ((e.getKeyCode() == KeyEvent.VK_Y) && (e.isControlDown()) && (!e.isConsumed())) {
      redoAction();
    }
  }

  /**
   * Code Implementation
   */
  private void createUndoMananger() {
    if (m_undoManager == null) {
      m_undoManager = new UndoManager();
      m_undoManager.setLimit(200);
    }
  }

  private void removeUndoMananger() {
    if (m_undoManager != null) {
      m_undoManager.end();
      m_undoManager = null;
    }
  }

  private void undoAction() {
    if (m_undoManager != null) {
      try {
        m_undoManager.undo();
      }
      catch (CannotUndoException cue) {
        Toolkit.getDefaultToolkit().beep();
      }
    }
  }

  private void redoAction() {
    if (m_undoManager != null) {
      try {
        m_undoManager.redo();
      }
      catch (CannotRedoException cue) {
        Toolkit.getDefaultToolkit().beep();
      }
    }
  }

}
