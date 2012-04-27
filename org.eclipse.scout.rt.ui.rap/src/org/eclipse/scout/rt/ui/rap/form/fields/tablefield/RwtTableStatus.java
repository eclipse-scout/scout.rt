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
package org.eclipse.scout.rt.ui.rap.form.fields.tablefield;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.LogicalGridData;
import org.eclipse.scout.rt.ui.rap.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.rap.util.RwtLayoutUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Default implementation using a target label
 */
public class RwtTableStatus implements IRwtTableStatus {
  static final String VARIANT_WIDETABLE_STATUS = "wideTableStatus";
  static final String VARIANT_TABLE_STATUS = "tableStatus";

  static final String VARIANT_POSTFIX_ERROR = "_error";
  static final String VARIANT_POSTFIX_WARNING = "_warning";

  private final Composite m_parent;
  private final IRwtEnvironment m_uiEnvironment;

  private final Composite m_labelContainer;
  private final Label m_populateLabel;
  private final Label m_selectionLabel;
  private boolean m_layoutNeeded;

  private String m_statusVariant = "";

  public RwtTableStatus(Composite parent, IRwtEnvironment uiEnvironment, ITableField<?> model) {
    m_parent = parent;
    m_uiEnvironment = uiEnvironment;

    m_labelContainer = getUiEnvironment().getFormToolkit().createComposite(m_parent);
    IForm form = model.getForm();
    if (IForm.VIEW_ID_PAGE_TABLE.equals(form.getDisplayViewId())) {
      m_statusVariant = VARIANT_WIDETABLE_STATUS;
    }
    else {
      m_statusVariant = VARIANT_TABLE_STATUS;
    }
    m_labelContainer.setData(WidgetUtil.CUSTOM_VARIANT, m_statusVariant);
    //Label 1
    m_populateLabel = getUiEnvironment().getFormToolkit().createLabel(m_labelContainer, "", SWT.NONE);
    m_populateLabel.setData(WidgetUtil.CUSTOM_VARIANT, m_statusVariant);
    m_populateLabel.setVisible(false);

    //Label 2
    m_selectionLabel = getUiEnvironment().getFormToolkit().createLabel(m_labelContainer, "", SWT.NONE);
    m_selectionLabel.setData(WidgetUtil.CUSTOM_VARIANT, m_statusVariant);
    m_selectionLabel.setVisible(false);

    //Layout
    LogicalGridData tableGridData = LogicalGridDataBuilder.createField(model.getGridData());
    LogicalGridData gd = new LogicalGridData();
    gd.gridx = tableGridData.gridx;
    gd.gridy = tableGridData.gridy + tableGridData.gridh;
    gd.gridw = tableGridData.gridw;
    gd.topInset = 2;
    gd.gridh = 1;
    gd.weightx = tableGridData.weightx;
    gd.weighty = 0.0;
    gd.fillHorizontal = true;
    m_labelContainer.setLayoutData(gd);

    GridLayout groupCompLayout = RwtLayoutUtility.createGridLayoutNoSpacing(1, false);
    m_labelContainer.setLayout(groupCompLayout);

    GridData popLabelLayoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
    popLabelLayoutData.exclude = true;
    popLabelLayoutData.verticalAlignment = SWT.CENTER;
    m_populateLabel.setLayoutData(popLabelLayoutData);

    GridData selLabelLayoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
    selLabelLayoutData.exclude = true;
    selLabelLayoutData.verticalAlignment = SWT.CENTER;
    m_selectionLabel.setLayoutData(selLabelLayoutData);
  }

  @Override
  public void dispose() {
    if (!m_populateLabel.isDisposed()) {
      m_populateLabel.dispose();
    }
    if (!m_selectionLabel.isDisposed()) {
      m_selectionLabel.dispose();
    }
    if (!m_labelContainer.isDisposed()) {
      m_labelContainer.dispose();
    }
  }

  private IRwtEnvironment getUiEnvironment() {
    return m_uiEnvironment;
  }

  @Override
  public void setStatus(IProcessingStatus populateStatus, IProcessingStatus selectionStatus) {
    setStatusImpl(m_populateLabel, populateStatus, true);
    setStatusImpl(m_selectionLabel, selectionStatus, true);

    if (!m_populateLabel.getVisible() && !m_selectionLabel.getVisible() && m_labelContainer.getVisible()) {
      m_layoutNeeded = true;
      m_labelContainer.setVisible(false);
    }
    else if ((m_populateLabel.getVisible() || m_selectionLabel.getVisible()) && !m_labelContainer.getVisible()) {
      m_layoutNeeded = true;
      m_labelContainer.setVisible(true);
    }

    if (m_layoutNeeded) {
      if (m_populateLabel.getVisible() && m_selectionLabel.getVisible() && m_labelContainer.getVisible()) {
        ((LogicalGridData) m_labelContainer.getLayoutData()).gridh = 2;
      }
      else if ((m_populateLabel.getVisible() || m_selectionLabel.getVisible()) && m_labelContainer.getVisible()) {
        ((LogicalGridData) m_labelContainer.getLayoutData()).gridh = 1;
      }
      else {
        ((LogicalGridData) m_labelContainer.getLayoutData()).gridh = 0;
      }

      m_layoutNeeded = false;
      m_parent.layout(true, true);
    }
  }

  private void setStatusImpl(Label field, IProcessingStatus status, boolean hideWhenNullStatus) {
    if (status == null) {
      field.setText("");
      if (hideWhenNullStatus) {
        if (field.getVisible()) {
          m_layoutNeeded = true;
          field.setVisible(false);
          ((GridData) field.getLayoutData()).exclude = true;
        }
      }
      return;
    }
    //
    if (!field.getVisible()) {
      m_layoutNeeded = true;
      field.setVisible(true);
      ((GridData) field.getLayoutData()).exclude = false;
    }
    String text = status.getMessage();
    //bsi ticket 95826: eliminate newlines
    if (text != null) {
      text = text.replaceAll("[\\s]+", " ");
    }
    field.setText(text != null ? " " + text : "");
    //style
    switch (status.getSeverity()) {
      case IProcessingStatus.ERROR:
      case IProcessingStatus.FATAL: {
        field.setData(WidgetUtil.CUSTOM_VARIANT, m_statusVariant + VARIANT_POSTFIX_ERROR);
        break;
      }
      case IProcessingStatus.WARNING:
      case IProcessingStatus.CANCEL: {
        field.setData(WidgetUtil.CUSTOM_VARIANT, m_statusVariant + VARIANT_POSTFIX_WARNING);
        break;
      }
      default: {
        field.setData(WidgetUtil.CUSTOM_VARIANT, m_statusVariant);
      }
    }
  }
}
