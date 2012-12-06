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
import org.eclipse.scout.rt.ui.rap.form.fields.datefield.IRwtScoutTimeField;
import org.eclipse.scout.rt.ui.rap.form.fields.datefield.chooser.TimeChooserDialog;
import org.eclipse.scout.rt.ui.rap.mobile.form.fields.datefield.chooser.MobileTimeChooserDialog;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @since 3.9.0
 */
public class RwtScoutMobileTimeField extends RwtScoutValueFieldComposite<IDateField> implements IRwtScoutTimeField {
  private final String VARIANT_TIMEFIELD_ICON = VARIANT_TIMEFIELD + "_icon";
  private final String VARIANT_TIMEFIELD_ICON_DISABLED = VARIANT_TIMEFIELD_ICON + "-disabled";

  private boolean m_ignoreLabel = false;
  private Composite m_timeContainer;
  private Composite m_iconContainer;
  private boolean m_dateTimeCompositeMember;
  private TimeChooserDialog m_timeChooserDialog = null;

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());

    m_timeContainer = getUiEnvironment().getFormToolkit().createComposite(container, SWT.BORDER);
    m_timeContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_TIMEFIELD);

    Text textField = new Text(m_timeContainer, SWT.NONE);
    getUiEnvironment().getFormToolkit().adapt(textField, false, false);
    textField.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_TIMEFIELD);

    m_iconContainer = getUiEnvironment().getFormToolkit().createComposite(m_timeContainer, SWT.NONE);
    m_iconContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_TIMEFIELD_ICON);

    setUiContainer(container);
    setUiLabel(label);
    setUiField(textField);

    // layout
    container.setLayout(new LogicalGridLayout(1, 0));

    m_timeContainer.setLayoutData(LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData()));
    GridLayout gridLayout = RwtLayoutUtility.createGridLayoutNoSpacing(2, false);
    //Paddings cannot be set in css because then mouse click won't work in that region
    gridLayout.marginLeft = 6;
    gridLayout.marginRight = 6;
    m_timeContainer.setLayout(gridLayout);

    GridData textLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, true);
    textField.setLayoutData(textLayoutData);

    GridData buttonLayoutData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
    buttonLayoutData.heightHint = 20;
    buttonLayoutData.widthHint = 20;
    m_iconContainer.setLayoutData(buttonLayoutData);

    textField.addMouseListener(new P_FieldSelectionListener());
    m_iconContainer.addMouseListener(new P_FieldSelectionListener());
    m_timeContainer.addMouseListener(new P_FieldSelectionListener());
  }

  @Override
  public Button getDropDownButton() {
    return null;
  }

  @Override
  public Text getUiField() {
    return (Text) super.getUiField();
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

    //Textfields are never disabled, see TextFieldEditableSupport.
    getUiField().setEnabled(true);
    getUiField().setEditable(false);

    if (b) {
      m_timeContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_TIMEFIELD);
      m_iconContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_TIMEFIELD_ICON);
    }
    else {
      m_timeContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_TIMEFIELD_DISABLED);
      m_iconContainer.setData(WidgetUtil.CUSTOM_VARIANT, VARIANT_TIMEFIELD_ICON_DISABLED);
    }
  }

  @Override
  protected void setFieldEnabled(Control field, boolean enabled) {
    // nop
  }

  @Override
  protected void setLabelVisibleFromScout() {
    if (!isIgnoreLabel()) {
      super.setLabelVisibleFromScout();
    }
  }

  @Override
  protected void setDisplayTextFromScout(String s) {
    IDateField scoutField = getScoutObject();
    if (s == null) {
      s = "";
    }
    Date value = scoutField.getValue();
    if (value != null) {
      DateFormat format = scoutField.getIsolatedTimeFormat();
      if (format != null) {
        s = format.format(value);
      }
    }
    getUiField().setText(s);
  }

  @Override
  protected void setBackgroundFromScout(String scoutColor) {
    setBackgroundFromScout(scoutColor, m_timeContainer);
  }

  protected void makeSureTimeChooserIsClosed() {
    if (m_timeChooserDialog != null
        && m_timeChooserDialog.getShell() != null
        && !m_timeChooserDialog.getShell().isDisposed()) {
      m_timeChooserDialog.getShell().close();
    }
  }

  private void handleUiTimeChooserAction() {
    Date oldTime = getScoutObject().getValue();
    if (oldTime == null) {
      oldTime = new Date();
    }

    makeSureTimeChooserIsClosed();
    m_timeChooserDialog = createTimeChooserDialog(getUiField().getShell(), oldTime);
    if (m_timeChooserDialog != null) {
      m_timeChooserDialog.getShell().addDisposeListener(new DisposeListener() {
        private static final long serialVersionUID = 1L;

        @Override
        public void widgetDisposed(DisposeEvent event) {
          m_timeChooserDialog.getShell().removeDisposeListener(this);
          getTimeFromClosedDateChooserDialog();
        }
      });

      m_timeChooserDialog.openTimeChooser(getUiField());
    }
  }

  protected MobileTimeChooserDialog createTimeChooserDialog(Shell parentShell, Date currentTime) {
    return new MobileTimeChooserDialog(parentShell, currentTime);
  }

  private void getTimeFromClosedDateChooserDialog() {
    final Date newDate = m_timeChooserDialog.getReturnTime();
    if (newDate != null) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().setTimeFromUI(newDate);
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

      handleUiTimeChooserAction();
    }

  }

}
