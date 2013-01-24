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
package org.eclipse.scout.rt.ui.swing.form.fields.groupbox;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.ext.BorderLayoutEx;
import org.eclipse.scout.rt.ui.swing.ext.FlowLayoutEx;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.form.fields.ISwingScoutFormField;

public class SwingScoutGroupBoxButtonbar implements ISwingScoutGroupBoxButtonbar {
  private IGroupBox m_scoutGroupBox;
  private ISwingEnvironment m_environment;

  private JPanel m_swingContainer;
  private JPanel m_leftButtonPart;

  private JComponent m_rightButtonPart;

  public void createField(IGroupBox scoutGroupBox, ISwingEnvironment environment) {
    m_scoutGroupBox = scoutGroupBox;
    m_environment = environment;
    m_swingContainer = new JPanelEx(new BorderLayoutEx(4, 0));
    m_swingContainer.setOpaque(false);
    if (!SwingUtility.isSynth()) {
      m_swingContainer.setBorder(UIManager.getBorder("GroupBoxButtonBar.border"));
    }
    //set synth name AFTER setting ui borders
    m_swingContainer.setName("Synth.GroupBoxButtonBar");
    int hgap = UIManager.getInt("GroupBoxButtonBar.horizontalGap");
    if (hgap <= 0) {
      hgap = 12;
    }
    m_leftButtonPart = new JPanelEx();
    m_leftButtonPart.setOpaque(false);
    m_leftButtonPart.setName("customProcessButtons");
    m_leftButtonPart.setLayout(new FlowLayoutEx(FlowLayoutEx.LEFT, hgap, 6));
    m_rightButtonPart = new JPanelEx();
    m_rightButtonPart.setOpaque(false);
    m_rightButtonPart.setName("systemProcessButtons");
    m_rightButtonPart.setLayout(new FlowLayoutEx(FlowLayoutEx.RIGHT, hgap, 6));
    //
    m_swingContainer.add(m_leftButtonPart, BorderLayoutEx.WEST);
    m_swingContainer.add(m_rightButtonPart, BorderLayoutEx.EAST);

    // buttons
    for (IFormField f : getScoutGroupBox().getFields()) {
      if (f instanceof IButton) {
        IButton b = (IButton) f;
        if (b.isProcessButton()) {
          if (b.getGridData().horizontalAlignment <= 0) {
            ISwingScoutFormField swingScoutComposite = getEnvironment().createFormField(m_leftButtonPart, b);
            m_leftButtonPart.add(swingScoutComposite.getSwingContainer());
          }
          else {
            ISwingScoutFormField swingScoutComposite = getEnvironment().createFormField(m_rightButtonPart, b);
            m_rightButtonPart.add(swingScoutComposite.getSwingContainer());
          }
        }
      }
    }
  }

  private void updateButtonbarVisibility() {
    m_swingContainer.doLayout();
  }

  public IGroupBox getScoutGroupBox() {
    return m_scoutGroupBox;
  }

  public ISwingEnvironment getEnvironment() {
    return m_environment;
  }

  @Override
  public JPanel getSwingContainer() {
    return m_swingContainer;
  }

  public JPanel getLeftButtonPart() {
    return m_leftButtonPart;
  }

  public JComponent getRightButtonPart() {
    return m_rightButtonPart;
  }
}
