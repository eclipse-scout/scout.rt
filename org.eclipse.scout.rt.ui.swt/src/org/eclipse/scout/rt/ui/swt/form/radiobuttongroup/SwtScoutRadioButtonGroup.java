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
package org.eclipse.scout.rt.ui.swt.form.radiobuttongroup;

import java.util.ArrayList;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.IRadioButtonGroup;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.ext.MultilineRadioButton;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.extension.IUiDecoration;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.swt.form.fields.ISwtScoutFormField;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.swt.form.radiobuttongroup.layout.RadioButtonGroupLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * <h3>SwtScoutRadioButtonGroup</h3> ...
 * 
 * @since 1.0.0 14.04.2008
 */
public class SwtScoutRadioButtonGroup extends SwtScoutValueFieldComposite<IRadioButtonGroup<?>> implements ISwtScoutRadioButtonGroup {

  private P_SwtButtonListener m_swtButtonListener = new P_SwtButtonListener();
  private ArrayList<MultilineRadioButton> m_swtRadioButtons = new ArrayList<MultilineRadioButton>();

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getEnvironment().getFormToolkit().createStatusLabel(container, getEnvironment(), getScoutObject());

    Composite buttonArea = new P_RadioButtonComposite(container);
    getEnvironment().getFormToolkit().adapt(buttonArea);
    for (IFormField scoutField : getScoutObject().getFields()) {
      ISwtScoutFormField swtField = getEnvironment().createFormField(buttonArea, scoutField);
      if (swtField.getSwtField() instanceof MultilineRadioButton) {
        MultilineRadioButton swtButton = (MultilineRadioButton) swtField.getSwtField();
        swtButton.addListener(SWT.Selection, m_swtButtonListener);
        swtButton.addListener(SWT.KeyDown, m_swtButtonListener);
        m_swtRadioButtons.add(swtButton);
        if (swtButton.getSelection()) {
          buttonArea.setTabList(new Control[]{swtButton.getParent().getParent()});
        }
      }
    }
    setSwtContainer(container);
    setSwtLabel(label);
    setSwtField(buttonArea);

    // layout
    getSwtContainer().setLayout(new LogicalGridLayout(1, 0));
    // button Area layout
    GridData scoutGridData = getScoutObject().getGridData();
    boolean usesLogicalGrid = getScoutObject().getGridRowCount() == scoutGridData.h && !scoutGridData.useUiHeight;
    if (usesLogicalGrid) {
      IUiDecoration deco = UiDecorationExtensionPoint.getLookAndFeel();
      getSwtField().setLayout(new RadioButtonGroupLayout(getScoutObject(), deco.getLogicalGridLayoutHorizontalGap(), deco.getLogicalGridLayoutVerticalGap()));
    }
    else {
      getSwtField().setLayout(new RadioButtonGroupLayout(getScoutObject(), 0, 4));
    }

  }

  @Override
  public Composite getSwtField() {
    return (Composite) super.getSwtField();
  }

  @Override
  protected void setBackgroundFromScout(String scoutColor) {
    if (getSwtField() != null) {
      Control fld = getSwtField();
      if (fld.getData(CLIENT_PROP_INITIAL_BACKGROUND) == null) {
        fld.setData(CLIENT_PROP_INITIAL_BACKGROUND, fld.getBackground());
      }
      Color initCol = (Color) fld.getData(CLIENT_PROP_INITIAL_BACKGROUND);
      Color c = getEnvironment().getColor(scoutColor);
      if (c == null) {
        c = initCol;
      }
      fld.setBackground(c);
      for (MultilineRadioButton b : m_swtRadioButtons) {
        b.setBackground(c);
      }
    }
  }

  private class P_SwtButtonListener implements Listener {
    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.Selection:
          if (event.widget instanceof MultilineRadioButton) {
            MultilineRadioButton button = (MultilineRadioButton) event.widget;
            handleSelectionChanged(button);
          }
          break;
        case SWT.KeyDown:
          handleKeyDown(event);
          break;
      }
    }

    private void handleSelectionChanged(MultilineRadioButton selectedButton) {
      getSwtField().setTabList(new Control[]{selectedButton.getParent()});
    }

    private void handleKeyDown(Event event) {
      int index = m_swtRadioButtons.indexOf(event.widget);
      if (index < 0) {
        return;
      }
      switch (event.keyCode) {
        case SWT.ARROW_DOWN:
        case SWT.ARROW_RIGHT:
          selectNextPossibleRadioButton(index);
          break;
        case SWT.ARROW_UP:
        case SWT.ARROW_LEFT:
          selectPreviousPossibleRadioButton(index);
          break;
        case SWT.HOME:
          selectFirstPossibleRadioButton();
          break;
        case SWT.END:
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
      for (int i = 1; i < m_swtRadioButtons.size(); i++) {
        int nextIndex = (currentIndex + i) % m_swtRadioButtons.size();
        if (m_swtRadioButtons.get(nextIndex).setFocus()) {
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
      for (int i = 1; i < m_swtRadioButtons.size(); i++) {
        int nextIndex = (currentIndex - i) % m_swtRadioButtons.size();
        if (nextIndex < 0) {
          nextIndex += m_swtRadioButtons.size();
        }
        if (m_swtRadioButtons.get(nextIndex).setFocus()) {
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
      for (int i = m_swtRadioButtons.size() - 1; i >= 0; i--) {
        if (m_swtRadioButtons.get(i).setFocus()) {
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
      for (MultilineRadioButton radioButton : m_swtRadioButtons) {
        if (radioButton.setFocus()) {
          return; //success
        }
      }
    }

  } // end class P_SwtButtonKeyListener

  private class P_RadioButtonComposite extends Composite {

    public P_RadioButtonComposite(Composite parent) {
      super(parent, SWT.NONE);
    }

    @Override
    protected void checkSubclass() {
    }

    @Override
    public boolean setFocus() {
      for (MultilineRadioButton b : m_swtRadioButtons) {
        if (b.getSelection()) {
          return b.getParent().setFocus();
        }
      }
      return super.setFocus();
    }
  }
}
