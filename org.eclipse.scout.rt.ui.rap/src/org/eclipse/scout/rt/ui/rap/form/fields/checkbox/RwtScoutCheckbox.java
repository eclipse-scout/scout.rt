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
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.core.LogicalGridData;
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
  private StatusLabelEx m_labelPlaceholder;
  //ticket 86811: avoid double-action in queue
  private boolean m_handleActionPending;

  @Override
  protected void initializeUi(Composite parent) {
    super.initializeUi(parent);
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    m_labelPlaceholder = new StatusLabelEx(container, SWT.NONE);
    getUiEnvironment().getFormToolkit().getFormToolkit().adapt(m_labelPlaceholder, false, false);
    m_labelPlaceholder.setLayoutData(LogicalGridDataBuilder.createLabel(getScoutObject().getGridData()));

    Button checkbox = getUiEnvironment().getFormToolkit().createButton(container, "", SWT.CHECK);

    LogicalGridData checkboxData = LogicalGridDataBuilder.createField(getScoutObject().getGridData());
    checkboxData.fillHorizontal = false;
    checkboxData.useUiWidth = true;
    checkboxData.weightx = 0;
    checkbox.setLayoutData(checkboxData);

    // This label is only used to dispatch some properties to the checkbox label (see updateLabel)
    // So it has to be invisible.
    StatusLabelEx dispatcherLabel = new StatusLabelEx(container, SWT.NONE);
    dispatcherLabel.setVisible(false);
    setUiLabel(dispatcherLabel);

    //
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

  /*
   * (non-Javadoc)
   * @see org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutValueFieldComposite#setErrorStatusFromScout(org.eclipse.scout.commons.exception.IProcessingStatus)
   */
  @Override
  protected void setErrorStatusFromScout(IProcessingStatus s) {
    // Update the status of the labelPlaceholder and not the dispatcherLabel
    m_labelPlaceholder.setStatus(s);
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutFieldComposite#setMandatoryFromScout(boolean)
   */
  @Override
  protected void setMandatoryFromScout(boolean b) {
    super.setMandatoryFromScout(b);
//    if (b != m_mandatoryCached) {
//      m_mandatoryCached = b;
//      getUiLabel().setMandatory(b); // bsh 2010-10-01: inform the label - some GUIs (e.g. Rayo) might use this information
//    }

    updateLabel();
  }

  /**
   * Updates the label of the checkbox with the properties of the dispatcher label.
   * This makes sure that the mandatory appearance is reflected correctly.
   */
  protected void updateLabel() {
    if (getUiLabel() instanceof StatusLabelEx) {
      StatusLabelEx uiLabel = getUiLabel();

      if (uiLabel.getDisplayText() != null) {
        getUiField().setText(uiLabel.getDisplayText());
      }

      getUiField().setFont(uiLabel.getFont());
      getUiField().setForeground(uiLabel.getForeground());
      getUiField().setBackground(uiLabel.getBackground());
    }
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
  public StatusLabelEx getPlaceholderLabel() {
    return m_labelPlaceholder;
  }

  @Override
  protected void setLabelVisibleFromScout() {
    boolean b = getScoutObject().isLabelVisible();
    if (m_labelPlaceholder != null && b != m_labelPlaceholder.getVisible()) {
      m_labelPlaceholder.setVisible(b);
      if (getUiContainer() != null && isCreated()) {
        getUiContainer().layout(true, true);
      }
    }
  }

  @Override
  protected void setLabelFromScout(String s) {
    super.setLabelFromScout(s);
    updateLabel();
  }

  @Override
  protected void setValueFromScout() {
    getUiField().setSelection(BooleanUtility.nvl(getScoutObject() == null ? null : getScoutObject().getValue()));
  }

  protected void handleUiAction() {
    if (getUiField().isEnabled()) {
      final boolean b = getUiField().getSelection();
      if (!m_handleActionPending) {
        m_handleActionPending = true;
        // notify Scout
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
        getUiEnvironment().invokeScoutLater(t, 0);
        // end notify
      }
    }
  }

  private class P_RwtButtonListener implements Listener {
    private static final long serialVersionUID = 1L;

    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.Selection:
          handleUiAction();
          break;
      }
    }
  } // end class P_RwtButtonListener

}
