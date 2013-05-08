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
package org.eclipse.scout.rt.ui.swing.ext.calendar;

import java.awt.AWTEvent;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Calendar;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.KeyStroke;

import org.eclipse.scout.rt.ui.swing.SwingUtility;

public abstract class AbstractCalendarCell extends JLabel {
  private static final long serialVersionUID = 1L;

  public AbstractCalendarCell() {
    // install focus handling and WHEN_FOCUSED input map (required since Labels
    // are per default non-focusable)
    setFocusable(true);
    setFocusTraversalPolicy(null);
    SwingUtility.installDefaultFocusHandling(this);

    // refresh when focused (border repaint)
    addFocusListener(new FocusListener() {
      @Override
      public void focusGained(FocusEvent e) {
        repaint();
        onFocusGained();
      }

      @Override
      public void focusLost(FocusEvent e) {
        repaint();
        onFocusLost();
      }
    });

    // SPACE selects cell and commits changes
    enableEvents(AWTEvent.KEY_EVENT_MASK);
    getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("SPACE"), "select");
    getActionMap().put("select", new AbstractAction() {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        onSpacePressed();
      }
    });
  }

  protected void onFocusGained() {
  }

  protected void onFocusLost() {
  }

  protected void onSpacePressed() {
  }

  public abstract Date getRepresentedDate();

  public abstract void setRepresentedState(Calendar c, boolean isMajor, boolean firstColumn, int displayType);

  public abstract boolean isSelected();

  public abstract void setSelected(boolean b);

  public abstract Object getItemAt(Point p);

  public abstract int getTimelessItemCount();

  public abstract int getTimedItemCount();

  public abstract void resetItemCache();

  public abstract void setWorkingHours(int startHour, int endHour, boolean useOverflowCells);

  public abstract void refresh();
}
