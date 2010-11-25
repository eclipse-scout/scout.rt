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
package org.eclipse.scout.rt.ui.swing.ext.calendar.tests;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.scout.rt.ui.swing.ext.calendar.DateTimeChooser;

/**
 *
 */
public final class TestDateTimeChooser {

  private TestDateTimeChooser() {
  }

  public static void main(String[] args) {
    final DateTimeChooser c = new DateTimeChooser();
    c.getDateChooser().addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        System.out.println("DATE CHANGED " + c.getDateChooser().getDate());
      }
    });
    c.getTimeChooser().addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        System.out.println("TIME CHANGED " + c.getTimeChooser().getTimeInMinutes() + " " + e.getActionCommand());
      }
    });
    //
    JFrame f = new JFrame();
    f.getContentPane().add(c.getContainer());
    f.pack();
    f.setLocationRelativeTo(null);
    f.setVisible(true);
  }
}
