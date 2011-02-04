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
package org.eclipse.scout.rt.ui.swing.ext;

import java.awt.AWTEvent;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.event.EventListenerList;

import org.eclipse.scout.rt.ui.swing.SwingUtility;

/**
 *
 */
public class JHyperlink extends JLabelEx {
  private static final long serialVersionUID = 1L;

  public JHyperlink(String text) {
    this();
    setText(text);
  }

  public JHyperlink() {
    setOpaque(false);
    setBorder(new HyperlinkBorder());
    setFocusable(true);
    setRequestFocusEnabled(true);
    setAlignmentX(0.5f);
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    SwingUtility.installDefaultFocusHandling(this);
    setName("Synth.Hyperlink");
    //
    getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("SPACE"), "action");
    getActionMap().put("action", new AbstractAction() {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        fireActionPerformed();
      }
    });
    //
    addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
        repaint();
      }

      public void focusLost(FocusEvent e) {
        repaint();
      }
    });
    addMouseListener(new MouseAdapter() {
      MouseClickedBugFix fix;

      @Override
      public void mouseEntered(MouseEvent e) {
        repaint();
      }

      @Override
      public void mouseExited(MouseEvent e) {
        repaint();
      }

      @Override
      public void mousePressed(MouseEvent e) {
        fix = new MouseClickedBugFix(e);
        if (isRequestFocusEnabled()) {
          requestFocus();
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if(fix!=null) fix.mouseReleased(this, e);
      }

      @Override
      public void mouseClicked(MouseEvent e) {
        if (fix.mouseClicked()) return;
        if (e.getClickCount() == 1) {
          fireActionPerformed();
        }
      }
    });
  }

  /**
   * Adds the specified action listener to receive
   * action events from this hyperlink
   * 
   * @param l
   *          the action listener to be added
   */
  public synchronized void addActionListener(ActionListener l) {
    listenerList.add(ActionListener.class, l);
  }

  /**
   * Removes the specified action listener so that it no longer
   * receives action events from this textfield.
   * 
   * @param l
   *          the action listener to be removed
   */
  public synchronized void removeActionListener(ActionListener l) {
    listenerList.remove(ActionListener.class, l);
  }

  /**
   * Notifies all listeners that have registered interest for
   * notification on this event type. The event instance
   * is lazily created.
   * The listener list is processed in last to
   * first order.
   * 
   * @see EventListenerList
   */
  protected void fireActionPerformed() {
    // Guaranteed to return a non-null array
    Object[] listeners = listenerList.getListenerList();
    int modifiers = 0;
    AWTEvent currentEvent = EventQueue.getCurrentEvent();
    if (currentEvent instanceof InputEvent) {
      modifiers = ((InputEvent) currentEvent).getModifiers();
    }
    else if (currentEvent instanceof ActionEvent) {
      modifiers = ((ActionEvent) currentEvent).getModifiers();
    }
    ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null, EventQueue.getMostRecentEventTime(), modifiers);
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ActionListener.class) {
        ((ActionListener) listeners[i + 1]).actionPerformed(e);
      }
    }
  }

}
