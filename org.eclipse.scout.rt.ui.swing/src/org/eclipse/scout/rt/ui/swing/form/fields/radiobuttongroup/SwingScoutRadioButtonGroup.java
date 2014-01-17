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
package org.eclipse.scout.rt.ui.swing.form.fields.radiobuttongroup;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.IRadioButtonGroup;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.form.fields.ISwingScoutFormField;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.swing.form.fields.radiobuttongroup.layout.RadioButtonGroupLayout;

public class SwingScoutRadioButtonGroup extends SwingScoutValueFieldComposite<IRadioButtonGroup<?>> implements ISwingScoutRadioButtonGroup {

  private ArrayList<JRadioButton> m_swingRadioButtons = new ArrayList<JRadioButton>();

  @Override
  protected void initializeSwing() {
    super.initializeSwing();
    JPanelEx container = new JPanelEx();
    container.setName("SwingScoutRadioButtonGroup.container");
    container.setOpaque(false);
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel(getScoutObject());
    container.add(label);
    JPanel buttonPanel = new JPanelEx();
    buttonPanel.setName("SwingScoutRadioButtonGroup.buttonPanel");
    buttonPanel.setOpaque(false);
    GridData scoutGridData = getScoutObject().getGridData();
    boolean usesLogicalGrid = (getScoutObject().getGridRowCount() == scoutGridData.h && !scoutGridData.useUiHeight);
    if (usesLogicalGrid) {
      buttonPanel.setLayout(new RadioButtonGroupLayout(getScoutObject(), getSwingEnvironment().getFormColumnGap(), getSwingEnvironment().getFormRowGap()));
    }
    else {
      buttonPanel.setLayout(new RadioButtonGroupLayout(getScoutObject(), 0, 0));
    }
    // add all radio buttons
    IFormField[] scoutFields = getScoutObject().getFields();
    for (int i = 0; i < scoutFields.length; i++) {
      ISwingScoutFormField comp = getSwingEnvironment().createFormField(buttonPanel, scoutFields[i]);
      buttonPanel.add(comp.getSwingContainer());
      if (comp.getSwingField() instanceof JRadioButton) {
        JRadioButton radioButton = (JRadioButton) comp.getSwingField();
        radioButton.addKeyListener(new P_SwingButtonKeyListener());
        m_swingRadioButtons.add(radioButton);
      }
    }
    container.add(buttonPanel);
    // register
    setSwingLabel(label);
    setSwingField(buttonPanel);
    setSwingContainer(container);
    // layout
    container.setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  @Override
  public JPanel getSwingRadioButtonPanel() {
    return (JPanel) getSwingField();
  }

  private class P_SwingButtonKeyListener extends KeyAdapter {
    @Override
    public void keyReleased(KeyEvent event) {
      int index = m_swingRadioButtons.indexOf(event.getComponent());
      if (index < 0) {
        return;
      }

      switch (event.getKeyCode()) {
        case KeyEvent.VK_DOWN:
        case KeyEvent.VK_RIGHT:
          selectNextPossibleRadioButton(index);
          break;
        case KeyEvent.VK_UP:
        case KeyEvent.VK_LEFT:
          selectPreviousPossibleRadioButton(index);
          break;
        case KeyEvent.VK_HOME:
          selectFirstPossibleRadioButton();
          break;
        case KeyEvent.VK_END:
          selectLastPossibleRadioButton();
          break;
      }

    }

    /**
     * Selects the next possible RadioButton based on the current index.
     * 
     * @since 3.10.0-M5
     */
    protected void selectNextPossibleRadioButton(int currentIndex) {
      for (int i = 1; i < m_swingRadioButtons.size(); i++) {
        int nextIndex = (currentIndex + i) % m_swingRadioButtons.size();
        JRadioButton newRadioButton = m_swingRadioButtons.get(nextIndex);
        if (acceptsFocus(newRadioButton)) {
          newRadioButton.setSelected(true);
          return; //success
        }
      }
    }

    /**
     * Selects the previous possible RadioButton based on the current index.
     * 
     * @since 3.10.0-M5
     */
    protected void selectPreviousPossibleRadioButton(int currentIndex) {
      for (int i = 1; i < m_swingRadioButtons.size(); i++) {
        int nextIndex = (currentIndex - i) % m_swingRadioButtons.size();
        if (nextIndex < 0) {
          nextIndex += m_swingRadioButtons.size();
        }
        JRadioButton newRadioButton = m_swingRadioButtons.get(nextIndex);
        if (acceptsFocus(newRadioButton)) {
          newRadioButton.setSelected(true);
          return; //success
        }
      }
    }

    /**
     * Selects the first possible RadioButton
     * 
     * @since 3.10.0-M5
     */
    protected void selectLastPossibleRadioButton() {
      for (int i = m_swingRadioButtons.size() - 1; i >= 0; i--) {
        JRadioButton newRadioButton = m_swingRadioButtons.get(i);
        if (newRadioButton.hasFocus()) {
          return;
        }
        if (acceptsFocus(newRadioButton)) {
          newRadioButton.setSelected(true);
          return; //success
        }
      }
    }

    /**
     * Selects the last possible RadioButton
     * 
     * @since 3.10.0-M5
     */
    protected void selectFirstPossibleRadioButton() {
      for (JRadioButton newRadioButton : m_swingRadioButtons) {
        if (newRadioButton.hasFocus()) {
          return;
        }
        if (acceptsFocus(newRadioButton)) {
          newRadioButton.setSelected(true);
          return; //success
        }
      }
    }

    /**
     * This method checks if the given {@link JRadioButton} can get the focus.
     * 
     * @param radioButton
     *          which shall get the focus
     * @return <code>true</code> if RadioButton accepts focus, <code>false</code> otherwise
     * @since 3.10.0-M5
     */
    protected boolean acceptsFocus(JRadioButton radioButton) {
      return radioButton.isFocusable() && radioButton.isVisible() && radioButton.isEnabled() && radioButton.requestFocusInWindow();
    }
  }
}
