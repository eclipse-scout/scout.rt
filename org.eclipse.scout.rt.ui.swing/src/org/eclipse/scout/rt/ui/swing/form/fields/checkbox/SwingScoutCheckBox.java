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
package org.eclipse.scout.rt.ui.swing.form.fields.checkbox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.SwingConstants;

import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.ext.JCheckBoxEx;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutValueFieldComposite;

public class SwingScoutCheckBox extends SwingScoutValueFieldComposite<IBooleanField> implements ISwingScoutCheckBox {
  private static final long serialVersionUID = 1L;

  private boolean m_mandatoryCached;
  //ticket 86811: avoid double-action in queue
  private boolean m_handleActionPending;

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx();
    container.setOpaque(false);
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel();
    container.add(label);
    JCheckBox swingCheckBox = createCheckBox(container);
    swingCheckBox.setOpaque(false);
    swingCheckBox.setVerifyInputWhenFocusTarget(true);
    swingCheckBox.setAlignmentX(0);
    swingCheckBox.setVerticalAlignment(SwingConstants.TOP);
    // attach swing listeners
    swingCheckBox.addActionListener(new P_SwingActionListener());
    //
    setSwingLabel(label);
    setSwingField(swingCheckBox);
    setSwingContainer(container);
    //bsi ticket 101344: modify the layout constraint for the checkbox, so it is only as wide as its label.
    //this avoids that clicking in white space area toggles the value
    LogicalGridData gd = (LogicalGridData) swingCheckBox.getClientProperty(LogicalGridData.CLIENT_PROPERTY_NAME);
    gd.fillHorizontal = false;
    // layout
    getSwingContainer().setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  protected JCheckBox createCheckBox(JComponent container) {
    JCheckBoxEx checkBox = new JCheckBoxEx();
    container.add(checkBox);
    return checkBox;
  }

  @Override
  public JCheckBoxEx getSwingCheckBox() {
    return (JCheckBoxEx) getSwingField();
  }

  @Override
  protected void setHorizontalAlignmentFromScout(int scoutAlign) {
    if (getSwingCheckBox() != null) {
      getSwingCheckBox().setHorizontalAlignment(SwingUtility.createHorizontalAlignment(scoutAlign));
    }
  }

  @Override
  protected void setLabelFromScout(String s) {
    getSwingCheckBox().setText(s);
  }

  @Override
  protected void setValueFromScout(Object o) {
    getSwingCheckBox().setSelected(((Boolean) o).booleanValue());
  }

  @Override
  protected void setMandatoryFromScout(boolean b) {
    if (b != m_mandatoryCached) {
      m_mandatoryCached = b;
      getSwingCheckBox().setMandatory(b);
      getSwingLabel().setMandatory(b); // bsh 2010-10-01: inform the label - some GUIs (e.g. Rayo) might use this information
    }
  }

  protected void handleSwingAction(ActionEvent e) {
    if (getSwingCheckBox().isEnabled()) {
      final boolean b = getSwingCheckBox().isSelected();
      if (!m_handleActionPending) {
        m_handleActionPending = true;
        //notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            try {
              getScoutObject().getUIFacade().setSelectedFromUI(b);
            }
            finally {
              m_handleActionPending = false;
            }
          }
        };
        getSwingEnvironment().invokeScoutLater(t, 0);
        //end notify
      }
    }
  }

  /*
   * Listeners
   */
  private class P_SwingActionListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      handleSwingAction(e);
    }
  }// end class
}
