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
package org.eclipse.scout.rt.ui.rap.mobile.form.fields.datefield;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.rap.form.fields.datefield.IRwtScoutDateField;
import org.eclipse.scout.rt.ui.rap.form.fields.datefield.chooser.DateChooserDialog;
import org.eclipse.scout.rt.ui.rap.mobile.form.fields.datefield.chooser.MobileDateChooserDialog;
import org.eclipse.scout.rt.ui.rap.util.RwtLayoutUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.9.0
 */
public class RwtScoutMobileDateField extends RwtScoutValueFieldComposite<IDateField> implements IRwtScoutDateField {
  private static final String VARIANT_DATEFIELD_ICON = VARIANT_DATEFIELD + "_icon";
  private static final String VARIANT_DATEFIELD_ICON_DISABLED = VARIANT_DATEFIELD_ICON + "-disabled";

  private boolean m_ignoreLabel = false;
  private Composite m_dateContainer;
  private Composite m_iconContainer;
  private boolean m_dateTimeCompositeMember;
  private DateChooserDialog m_dateChooserDialog = null;

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());

    m_dateContainer = getUiEnvironment().getFormToolkit().createComposite(container, SWT.BORDER);
    m_dateContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_DATEFIELD);

    Label textField = new Label(m_dateContainer, SWT.NONE);
    getUiEnvironment().getFormToolkit().adapt(textField, false, false);
    textField.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_DATEFIELD);

    m_iconContainer = getUiEnvironment().getFormToolkit().createComposite(m_dateContainer);
    m_iconContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_DATEFIELD_ICON);

    setUiContainer(container);
    setUiLabel(label);
    setUiField(textField);

    container.setLayout(new LogicalGridLayout(1, 0));

    m_dateContainer.setLayoutData(LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData()));
    GridLayout gridLayout = RwtLayoutUtility.createGridLayoutNoSpacing(2, false);
    //Paddings cannot be set in css because then mouse click won't work in that region
    gridLayout.marginLeft = 5;
    gridLayout.marginRight = 5;
    m_dateContainer.setLayout(gridLayout);

    GridData textLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, true);
    textLayoutData.verticalIndent = 1;
    textField.setLayoutData(textLayoutData);

    GridData buttonLayoutData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
    buttonLayoutData.heightHint = 20;
    buttonLayoutData.widthHint = 20;
    m_iconContainer.setLayoutData(buttonLayoutData);

    textField.addMouseListener(new P_FieldSelectionListener());
    m_iconContainer.addMouseListener(new P_FieldSelectionListener());
    m_dateContainer.addMouseListener(new P_FieldSelectionListener());
  }

  @Override
  public Button getDropDownButton() {
    return null;
  }

  @Override
  public Label getUiField() {
    return (Label) super.getUiField();
  }

  @Override
  public void setIgnoreLabel(boolean ignoreLabel) {
    m_ignoreLabel = ignoreLabel;
    if (ignoreLabel) {
      getUiLabel().setVisible(false);
    }
    else {
      getUiLabel().setVisible(getScoutObject().isLabelVisible());
    }
  }

  public boolean isIgnoreLabel() {
    return m_ignoreLabel;
  }

  public boolean isDateTimeCompositeMember() {
    return m_dateTimeCompositeMember;
  }

  @Override
  public void setDateTimeCompositeMember(boolean dateTimeCompositeMember) {
    m_dateTimeCompositeMember = dateTimeCompositeMember;
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);

    if (b) {
      m_dateContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_DATEFIELD);
      m_iconContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_DATEFIELD_ICON);
    }
    else {
      m_dateContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_DATEFIELD_DISABLED);
      m_iconContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_DATEFIELD_ICON_DISABLED);
    }
  }

  @Override
  protected void setFieldEnabled(Control field, boolean enabled) {
    //Textfields are never disabled, see TextFieldEditableSupport.
    super.setFieldEnabled(field, true);
  }

  @Override
  protected void setLabelVisibleFromScout() {
    if (!isIgnoreLabel()) {
      super.setLabelVisibleFromScout();
    }
  }

  @Override
  protected void setDisplayTextFromScout(String s) {
    IDateField f = getScoutObject();
    if (f.getErrorStatus() != null) {
      return;
    }
    if (s == null) {
      s = "";
    }
    Date value = f.getValue();
    if (value == null) {
      //only date field
      getUiField().setText(s);
    }
    else {
      DateFormat format = f.getIsolatedDateFormat();
      if (format != null) {
        s = format.format(value);
        getUiField().setText(s);
      }
    }
  }

  @Override
  protected void setBackgroundFromScout(String scoutColor) {
    setBackgroundFromScout(scoutColor, m_dateContainer);
  }

  protected void makeSureDateChooserIsClosed() {
    if (m_dateChooserDialog != null
        && m_dateChooserDialog.getShell() != null
        && !m_dateChooserDialog.getShell().isDisposed()) {
      m_dateChooserDialog.getShell().close();
    }
  }

  private void handleUiDateChooserAction() {
    Date oldDate = getScoutObject().getValue();
    if (oldDate == null) {
      oldDate = new Date();
    }

    makeSureDateChooserIsClosed();
    m_dateChooserDialog = createDateChooserDialog(getUiField().getShell(), oldDate);
    if (m_dateChooserDialog != null) {

      m_dateChooserDialog.getShell().addDisposeListener(new DisposeListener() {
        private static final long serialVersionUID = 1L;

        @Override
        public void widgetDisposed(DisposeEvent event) {
          m_dateChooserDialog.getShell().removeDisposeListener(this);
          getDateFromClosedDateChooserDialog();
        }
      });

      m_dateChooserDialog.openDateChooser(getUiField());
    }
  }

  protected MobileDateChooserDialog createDateChooserDialog(Shell parentShell, Date currentDate) {
    return new MobileDateChooserDialog(parentShell, currentDate);
  }

  private void getDateFromClosedDateChooserDialog() {
    final Date newDate = m_dateChooserDialog.getReturnDate();
    if (newDate != null) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().setDateFromUI(newDate);
        }
      };
      getUiEnvironment().invokeScoutLater(t, 0);
      // end notify
    }
  }

  private class P_FieldSelectionListener extends MouseAdapter {
    private static final long serialVersionUID = 1L;

    @Override
    public void mouseUp(MouseEvent e) {
      if (!getScoutObject().isEnabled()) {
        return;
      }

      handleUiDateChooserAction();
    }

  }

}
