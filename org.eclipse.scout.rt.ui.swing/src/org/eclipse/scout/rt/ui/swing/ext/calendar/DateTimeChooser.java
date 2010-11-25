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

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.eclipse.scout.rt.ui.swing.ext.FlowLayoutEx;
import org.eclipse.scout.rt.ui.swing.ext.JHyperlink;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;

public class DateTimeChooser {
  private JPanel m_container;
  private DateChooser m_dateChooser;
  private TimeChooser m_timeChooser;
  private JComponent m_applyButton;

  public DateTimeChooser() {
    m_dateChooser = new DateChooser();
    m_timeChooser = new TimeChooser();
    String applyText = UIManager.getString("DateChooser.applyButtonText");
    if (applyText == null) {
      applyText = "Apply";
    }
    JHyperlink applyButton = new JHyperlink(applyText);
    applyButton.setRequestFocusEnabled(false);
    m_applyButton = applyButton;
    //
    m_container = new JPanelEx();
    JPanelEx vPanel = new JPanelEx();
    JPanelEx buttonPanel = new JPanelEx(new FlowLayoutEx(FlowLayoutEx.HORIZONTAL, FlowLayoutEx.RIGHT, 0, 0));
    buttonPanel.add(applyButton);
    vPanel.add(m_dateChooser.getContainer(), BorderLayout.CENTER);
    vPanel.add(buttonPanel, BorderLayout.SOUTH);
    m_container.add(vPanel, BorderLayout.WEST);
    m_container.add(m_timeChooser.getContainer(), BorderLayout.EAST);
  }

  public DateChooser getDateChooser() {
    return m_dateChooser;
  }

  public TimeChooser getTimeChooser() {
    return m_timeChooser;
  }

  public JComponent getApplyButton() {
    return m_applyButton;
  }

  public JPanel getContainer() {
    return m_container;
  }

}
