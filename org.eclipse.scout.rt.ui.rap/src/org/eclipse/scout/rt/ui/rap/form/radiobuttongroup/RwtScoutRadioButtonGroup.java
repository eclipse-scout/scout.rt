/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.form.radiobuttongroup;

import java.util.ArrayList;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.IRadioButtonGroup;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.ext.ButtonEx;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.extension.IUiDecoration;
import org.eclipse.scout.rt.ui.rap.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.rap.form.radiobuttongroup.layout.RadioButtonGroupLayout;
import org.eclipse.scout.rt.ui.rap.keystroke.RwtKeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

/**
 * <h3>RwtScoutRadioButtonGroup</h3> ...
 * 
 * @since 3.7.0 June 2011
 */
public class RwtScoutRadioButtonGroup extends RwtScoutValueFieldComposite<IRadioButtonGroup<?>> implements IRwtScoutRadioButtonGroup {

  private ArrayList<Button> m_uiRadioButtons = new ArrayList<Button>();

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());

    Composite buttonArea = getUiEnvironment().getFormToolkit().createComposite(container);
    getUiEnvironment().getFormToolkit().adapt(buttonArea);
    for (IFormField scoutField : getScoutObject().getFields()) {
      IRwtScoutFormField uiField = getUiEnvironment().createFormField(buttonArea, scoutField);
      if (uiField.getUiField() instanceof Button) {
        Button uiButton = (Button) uiField.getUiField();
        getUiEnvironment().addKeyStroke(uiButton, new P_KeyStroke(SWT.ARROW_DOWN), false);
        getUiEnvironment().addKeyStroke(uiButton, new P_KeyStroke(SWT.ARROW_RIGHT), false);
        getUiEnvironment().addKeyStroke(uiButton, new P_KeyStroke(SWT.ARROW_UP), false);
        getUiEnvironment().addKeyStroke(uiButton, new P_KeyStroke(SWT.ARROW_LEFT), false);
        getUiEnvironment().addKeyStroke(uiButton, new P_KeyStroke(SWT.HOME), false);
        getUiEnvironment().addKeyStroke(uiButton, new P_KeyStroke(SWT.END), false);

        m_uiRadioButtons.add(uiButton);
      }
    }
    setUiContainer(container);
    setUiLabel(label);
    setUiField(buttonArea);

    // layout
    getUiContainer().setLayout(new LogicalGridLayout(1, 0));
    // button Area layout
    GridData scoutGridData = getScoutObject().getGridData();
    boolean usesLogicalGrid = getScoutObject().getGridRowCount() == scoutGridData.h && !scoutGridData.useUiHeight;
    if (usesLogicalGrid) {
      IUiDecoration deco = UiDecorationExtensionPoint.getLookAndFeel();
      getUiField().setLayout(new RadioButtonGroupLayout(getScoutObject(), deco.getLogicalGridLayoutHorizontalGap(), deco.getLogicalGridLayoutVerticalGap()));
    }
    else {
      getUiField().setLayout(new RadioButtonGroupLayout(getScoutObject(), 0, 4));
    }
  }

  @Override
  public Composite getUiField() {
    return (Composite) super.getUiField();
  }

  private class P_KeyStroke extends RwtKeyStroke {
    public P_KeyStroke(int keyCode) {
      super(keyCode);
    }

    @Override
    public void handleUiAction(Event e) {
      if (e.widget instanceof Button) {
        handleKeyEvent(e);
      }
    }

    public void handleKeyEvent(Event e) {
      int index = m_uiRadioButtons.indexOf(e.widget);
      if (index < 0) {
        return;
      }
      switch (e.keyCode) {
        case SWT.ARROW_DOWN:
        case SWT.ARROW_RIGHT:
          selectNextPossibleRadioButton(index, e);
          break;
        case SWT.ARROW_UP:
        case SWT.ARROW_LEFT:
          selectPreviousPossibleRadioButton(index, e);
          break;
        case SWT.HOME:
          selectFirstPossibleRadioButton(index, e);
          break;
        case SWT.END:
          selectLastPossibleRadioButton(index, e);
          break;
      }
      e.doit = false;
    }

    /**
     * Selects the next possible RadioButton based on the current index.
     * 
     * @since 3.10.0-M5
     */
    protected void selectNextPossibleRadioButton(int currentIndex, Event e) {
      Button oldButton = m_uiRadioButtons.get(currentIndex);

      for (int i = 1; i < m_uiRadioButtons.size(); i++) {
        int nextIndex = (currentIndex + i) % m_uiRadioButtons.size();
        Button newButton = m_uiRadioButtons.get(nextIndex);
        if (newButton.setFocus()) {
          selectButton(newButton, oldButton, e);
          return; //success
        }
      }
    }

    /**
     * Selects the previous possible RadioButton based on the current index.
     * 
     * @since 3.10.0-M5
     */
    protected void selectPreviousPossibleRadioButton(int currentIndex, Event e) {
      Button oldButton = m_uiRadioButtons.get(currentIndex);

      for (int i = 1; i < m_uiRadioButtons.size(); i++) {
        int nextIndex = (currentIndex - i) % m_uiRadioButtons.size();
        if (nextIndex < 0) {
          nextIndex += m_uiRadioButtons.size();
        }

        Button newButton = m_uiRadioButtons.get(nextIndex);
        if (newButton.setFocus()) {
          selectButton(newButton, oldButton, e);
          return; //success
        }
      }
    }

    /**
     * Selects the first possible RadioButton
     * 
     * @since 3.10.0-M5
     */
    protected void selectLastPossibleRadioButton(int currentIndex, Event e) {
      Button oldButton = m_uiRadioButtons.get(currentIndex);

      for (int i = m_uiRadioButtons.size() - 1; i >= 0; i--) {
        Button newButton = m_uiRadioButtons.get(i);
        if (newButton.setFocus()) {
          selectButton(newButton, oldButton, e);
          return; //success
        }
      }
    }

    /**
     * Selects the last possible RadioButton
     * 
     * @since 3.10.0-M5
     */
    protected void selectFirstPossibleRadioButton(int currentIndex, Event e) {
      Button oldButton = m_uiRadioButtons.get(currentIndex);

      for (Button newButton : m_uiRadioButtons) {
        if (newButton.setFocus()) {
          selectButton(newButton, oldButton, e);
          return; //success
        }
      }
    }

    /**
     * Selects the new button and deselects the old one
     * 
     * @since 3.10.0-M5
     */
    private void selectButton(Button newButton, Button oldButton, Event e) {
      oldButton.setSelection(false);
      newButton.setSelection(true);
      if (newButton instanceof ButtonEx) {
        ((ButtonEx) newButton).handleButtonSelectionFromKeyStroke(e);
      }
    }
  }

}
