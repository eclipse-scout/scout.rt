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
package org.eclipse.scout.rt.ui.rap.form.fields.datefield;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.core.LogicalGridData;
import org.eclipse.scout.rt.ui.rap.core.ext.ILabelComposite;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.form.fields.IPopupSupport;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutValueFieldComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class RwtScoutDateTimeCompositeField extends RwtScoutValueFieldComposite<IDateField> implements IRwtScoutFormField<IDateField>, IPopupSupport {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutDateTimeCompositeField.class);

  private RwtScoutDateField m_dateField;
  private RwtScoutTimeField m_timeField;

  private Set<IPopupSupportListener> m_popupEventListeners;
  private Object m_popupEventListenerLock;

  @Override
  protected void initializeUi(Composite parent) {
    m_popupEventListeners = new HashSet<IPopupSupportListener>();
    m_popupEventListenerLock = new Object();
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent);
    StatusLabelEx label = getUiEnvironment().getFormToolkit().createStatusLabel(container, getScoutObject());

    Composite fieldContainer = getUiEnvironment().getFormToolkit().createComposite(container);

    m_dateField = new RwtScoutDateField();
    m_dateField.createUiField(fieldContainer, getScoutObject(), getUiEnvironment());
    m_dateField.getUiContainer().setLayoutData(createDateFieldGridData());
    m_dateField.setIgnoreLabel(true);
    m_dateField.setDateTimeCompositeMember(true);
    // remove label fixed width hint
    ILabelComposite childLabel = m_dateField.getUiLabel();
    if (childLabel != null && childLabel.getLayoutData() instanceof LogicalGridData) {
      ((LogicalGridData) childLabel.getLayoutData()).widthHint = 0;
    }

    m_timeField = new RwtScoutTimeField();
    m_timeField.createUiField(fieldContainer, getScoutObject(), getUiEnvironment());
    m_timeField.getUiContainer().setLayoutData(createTimeFieldGridData());
    m_timeField.setIgnoreLabel(true);
    m_timeField.setDateTimeCompositeMember(true);
    // remove label fixed width hint
    childLabel = m_timeField.getUiLabel();
    if (childLabel != null && childLabel.getLayoutData() instanceof LogicalGridData) {
      ((LogicalGridData) childLabel.getLayoutData()).widthHint = 0;
    }

    //
    setUiContainer(container);
    setUiField(fieldContainer);
    setUiLabel(label);
    // layout
    fieldContainer.setLayout(new LogicalGridLayout(6, 0));
    container.setLayout(new LogicalGridLayout(1, 0));
  }

  @Override
  protected void setFieldEnabled(Control rwtField, boolean enabled) {
    m_dateField.setFieldEnabled(rwtField, enabled);
    m_timeField.setFieldEnabled(rwtField, enabled);
  }

  protected LogicalGridData createDateFieldGridData() {
    LogicalGridData data = new LogicalGridData();
    data.gridx = 1;
    data.gridy = 0;
    data.gridw = 1;
    data.gridh = 1;
    data.weightx = 1.0;
    data.weighty = 0;
    data.useUiWidth = true;
    return data;
  }

  protected LogicalGridData createTimeFieldGridData() {
    LogicalGridData data = new LogicalGridData();
    data.gridx = 2;
    data.gridy = 0;
    data.gridw = 1;
    data.gridh = 1;
    data.weightx = 1.0;
    data.weighty = 0;
    data.useUiWidth = true;
    return data;
  }

  @Override
  public void addPopupEventListener(IPopupSupportListener listener) {
    m_dateField.addPopupEventListener(listener);
    m_timeField.addPopupEventListener(listener);
  }

  @Override
  public void removePopupEventListener(IPopupSupportListener listener) {
    m_dateField.removePopupEventListener(listener);
    m_timeField.removePopupEventListener(listener);
  }
}
