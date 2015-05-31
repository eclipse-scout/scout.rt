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
package org.eclipse.scout.rt.ui.rap.form.fields.checkbox;

import org.eclipse.scout.commons.BooleanUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.ui.rap.LogicalGridData;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutValueFieldComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class RwtScoutCheckbox extends RwtScoutValueFieldComposite<IBooleanField> implements IRwtScoutCheckbox {

  private P_RwtButtonListener m_uiButtonListener;
  private boolean m_mandatoryCached;

  @Override
  protected void initializeUi(Composite parent) {
    super.initializeUi(parent);
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);

    // The label is used to visualize error and mandatory status.
    StatusLabelEx label = new StatusLabelEx(container, SWT.NONE);
    getUiEnvironment().getFormToolkit().getFormToolkit().adapt(label, false, false);
    label.setLayoutData(LogicalGridDataBuilder.createLabel(getScoutObject().getGridData()));

    // Create the checkbox.
    Button checkbox = getUiEnvironment().getFormToolkit().createButton(container, "", SWT.CHECK | SWT.WRAP);
    LogicalGridData gd = LogicalGridDataBuilder.createField(getScoutObject().getGridData());
    gd.useUiWidth = true;
    gd.weightx = 0;
    checkbox.setLayoutData(gd);

    setUiLabel(label);

    setUiContainer(container);
    setUiField(checkbox);

    // layout
    container.setLayout(new LogicalGridLayout(1, 0));
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    if (m_uiButtonListener == null) {
      m_uiButtonListener = new P_RwtButtonListener();
    }
    getUiField().addListener(SWT.Selection, m_uiButtonListener);
  }

  @Override
  protected void detachScout() {
    super.detachScout();
    getUiField().removeListener(SWT.Selection, m_uiButtonListener);
  }

  @Override
  public Button getUiField() {
    return (Button) super.getUiField();
  }

  @Override
  public StatusLabelEx getUiLabel() {
    return (StatusLabelEx) super.getUiLabel();
  }

  @Override
  protected void setLabelFromScout(String s) {
    getUiField().setText(StringUtility.nvl(s, ""));
  }

  @Override
  protected void setValueFromScout() {
    getUiField().setSelection(BooleanUtility.nvl(getScoutObject() == null ? null : getScoutObject().getValue()));
  }

  protected void handleUiAction(final boolean selection) {
    if (!getUiField().isEnabled()) {
      return;
    }
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().setSelectedFromUI(selection);
        // ensure the selection state of model and UI matches.
        if (selection != getScoutObject().isChecked()) {
          Runnable r = new Runnable() {
            @Override
            public void run() {
              getUiField().setSelection(getScoutObject().isChecked());
            }
          };
          getUiEnvironment().invokeUiLater(r);
        }
      }
    };
    getUiEnvironment().invokeScoutLater(t, 0);
    // end notify
  }

  private class P_RwtButtonListener implements Listener {
    private static final long serialVersionUID = 1L;

    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.Selection:
          handleUiAction(getUiField().getSelection());
          break;
      }
    }
  } // end class P_RwtButtonListener

}
