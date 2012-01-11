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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * <h3>RwtScoutRadioButtonGroup</h3> ...
 * 
 * @since 3.7.0 June 2011
 */
public class RwtScoutRadioButtonGroup extends RwtScoutValueFieldComposite<IRadioButtonGroup<?>> implements IRwtScoutRadioButtonGroup {

  private P_RwtButtonListener m_uiButtonListener = new P_RwtButtonListener();
  private ArrayList<Button> m_uiRadioButtons = new ArrayList<Button>();

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    int labelStyle = UiDecorationExtensionPoint.getLookAndFeel().getFormFieldLabelAlignment();
    StatusLabelEx label = new StatusLabelEx(container, labelStyle);
    getUiEnvironment().getFormToolkit().getFormToolkit().adapt(label, false, false);
    Composite buttonArea = new P_RadioButtonComposite(container);
    getUiEnvironment().getFormToolkit().adapt(buttonArea);
    for (IFormField scoutField : getScoutObject().getFields()) {
      IRwtScoutFormField uiField = getUiEnvironment().createFormField(buttonArea, scoutField);
      if (uiField.getUiField() instanceof Button) {
        Button uiButton = (Button) uiField.getUiField();
        uiButton.addListener(SWT.Selection, m_uiButtonListener);
        getUiEnvironment().addKeyStroke(uiButton, new P_KeyStroke(SWT.ARROW_DOWN), false);
        getUiEnvironment().addKeyStroke(uiButton, new P_KeyStroke(SWT.ARROW_RIGHT), false);
        getUiEnvironment().addKeyStroke(uiButton, new P_KeyStroke(SWT.ARROW_UP), false);
        getUiEnvironment().addKeyStroke(uiButton, new P_KeyStroke(SWT.ARROW_LEFT), false);
        getUiEnvironment().addKeyStroke(uiButton, new P_KeyStroke(SWT.HOME), false);
        getUiEnvironment().addKeyStroke(uiButton, new P_KeyStroke(SWT.END), false);

        m_uiRadioButtons.add(uiButton);
        if (uiButton.getSelection()) {
          buttonArea.setTabList(new Control[]{uiButton.getParent()});
        }
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

  @Override
  protected void setFocusRequestedFromScout(boolean b) {
    Control[] tabList = getUiField().getTabList();
    if (b && tabList != null && tabList.length > 0) {
      tabList[0].setFocus();
    }
  }

  private class P_RwtButtonListener implements Listener {
    private static final long serialVersionUID = 1L;

    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.Selection:
          Button button = (Button) event.widget;
          handleSelectionChanged(button);
          break;
      }
    }

    private void handleSelectionChanged(Button selectedButton) {
      getUiField().setTabList(new Control[]{selectedButton.getParent()});
    }

  } // end class P_RwtButtonKeyListener

  private class P_KeyStroke extends RwtKeyStroke {
    public P_KeyStroke(int keyCode) {
      super(keyCode);
    }

    @Override
    public void handleUiAction(Event e) {
      int index = m_uiRadioButtons.indexOf(e.widget);
      switch (e.keyCode) {
        case SWT.ARROW_DOWN:
        case SWT.ARROW_RIGHT:
          index++;
          break;
        case SWT.ARROW_UP:
        case SWT.ARROW_LEFT:
          // ensure not -1
          index = index + m_uiRadioButtons.size() - 1;
          break;
        case SWT.HOME:
          index = 0;
          break;
        case SWT.END:
          index = m_uiRadioButtons.size() - 1;
          break;
      }
      index = index % m_uiRadioButtons.size();
      m_uiRadioButtons.get(index).setFocus();
    }
  }

  private class P_RadioButtonComposite extends Composite {
    private static final long serialVersionUID = 1L;

    public P_RadioButtonComposite(Composite parent) {
      super(parent, SWT.NONE);
    }

    @Override
    protected void checkSubclass() {
    }

    @Override
    public boolean setFocus() {
      for (Button b : m_uiRadioButtons) {
        if (b.getSelection()) {
          return b.getParent().setFocus();
        }
      }
      return super.setFocus();
    }
  }
}
