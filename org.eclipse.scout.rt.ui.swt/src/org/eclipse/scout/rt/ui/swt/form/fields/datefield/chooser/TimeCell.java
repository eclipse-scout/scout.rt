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
package org.eclipse.scout.rt.ui.swt.form.fields.datefield.chooser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

/**
 * Class SwtTimeCell. The time cell represents a label of a daytime used in the
 * UiTimeChooserDialogContent.
 */
public class TimeCell {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(TimeCell.class);
  private Composite m_container = null;

  private Label m_timeLabel = null;

  private Label m_iconArea = null;

  private Date m_cellDate = null;

  private Color m_defaultBackground;

  private AbstractDateSelectionListener m_dateChangedListener = null;
  private final ISwtEnvironment m_environment;

  /**
   * Constructor for SwtTimeCell
   * 
   * @param parent
   *          the parent composite
   * @param listener
   *          the listner which will be notified once a selection of this field
   *          occures.
   */
  public TimeCell(Composite parent, AbstractDateSelectionListener listener, ISwtEnvironment environment) {
    m_dateChangedListener = listener;
    m_environment = environment;
    m_container = new Composite(parent, SWT.NONE);
    m_iconArea = new Label(m_container, SWT.NONE);
    m_iconArea.setBackground(m_iconArea.getShell().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    m_timeLabel = new Label(m_container, SWT.CENTER);
    m_timeLabel.setBackground(getEnvironment().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    P_TimeSelectionListener timeSelectionListener = new P_TimeSelectionListener();
    m_timeLabel.addMouseListener(timeSelectionListener);
    m_iconArea.addMouseListener(timeSelectionListener);
    P_MouseOverListener mouseTrackListener = new P_MouseOverListener();
    m_iconArea.addMouseTrackListener(mouseTrackListener);
    m_timeLabel.addMouseTrackListener(mouseTrackListener);
  }

  /**
   * @param type
   *          the listener type
   * @param listener
   *          the listener will be added to the cell.
   */
  public void addListener(int type, Listener listener) {
    m_timeLabel.addListener(type, listener);
    m_iconArea.addListener(type, listener);
  }

  private void layout(int labelWith) {
    GridData containerData = new GridData();
    containerData.grabExcessVerticalSpace = true;
    containerData.grabExcessHorizontalSpace = true;
    containerData.horizontalAlignment = SWT.FILL;
    containerData.verticalAlignment = GridData.FILL;
    m_container.setLayoutData(containerData);
    FormLayout containerLayout = new FormLayout();
    // containerLayout.marginHeight=20;
    // containerLayout.marginWidth=50;
    m_container.setLayout(containerLayout);
    FormData iconData = new FormData();
    iconData.left = new FormAttachment(0, 0);
    iconData.top = new FormAttachment(0, 0);
    iconData.bottom = new FormAttachment(100, 0);
    m_iconArea.setLayoutData(iconData);
    FormData labelData = new FormData();
    labelData.width = labelWith;
    labelData.top = new FormAttachment(0, 0);
    labelData.bottom = new FormAttachment(100, 0);
    labelData.right = new FormAttachment(100, 0);
    labelData.left = new FormAttachment(m_iconArea, 0);
    m_timeLabel.setLayoutData(labelData);
  }

  /**
   * to set the text of the cell.
   * 
   * @param c
   *          the current time
   * @param dateEquality
   *          says if the current time is is exactly the highlighted or within
   *          the highlighted hour.
   */
  public void setRepresentedState(Calendar c, DateEquality dateEquality) {
    m_cellDate = c.getTime();
    int labelWith = 30;
    setSelection(dateEquality);
    if (c.get(Calendar.MINUTE) == 0) {
      labelWith = 50;
      m_iconArea.setImage(getEnvironment().getIcon(AbstractIcons.DateFieldTime));
      m_timeLabel.setText(new SimpleDateFormat("HH:mm").format(m_cellDate));
    }
    else {
      m_timeLabel.setText(new SimpleDateFormat(":mm").format(m_cellDate));
    }
    layout(labelWith);
  }

  /*
   * helper to highlight the selected day time.
   */
  private void setSelection(DateEquality dateEquality) {
    if (dateEquality.equals(DateEquality.HOUR_EQUAL)) {
      m_timeLabel.setBackground(getEnvironment().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    }
    if (dateEquality.equals(DateEquality.QUARTER_EQUAL)) {
      Font f = getEnvironment().getFont(new FontSpec(null, FontSpec.STYLE_BOLD, -1), m_timeLabel.getFont());
      m_timeLabel.setFont(f);
      m_timeLabel.setBackground(m_timeLabel.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
      m_timeLabel.setForeground(m_timeLabel.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
    }
  }

  private void setStyle(boolean mouseOver) {
    if (mouseOver) {
      m_defaultBackground = m_timeLabel.getBackground();
      m_timeLabel.setBackground(m_timeLabel.getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
    }
    else {
      m_timeLabel.setBackground(m_defaultBackground);
    }
  }

  /**
   * aware that only one listener can be added to a cell.
   * 
   * @param listener
   *          the listener will be notified once a selection occures on this
   *          field.
   * @throws ListenerRegisterException
   *           the exception will be thrown if already a listener is added.
   */
  public void registerTimeChangedListener(AbstractDateSelectionListener listener) {
    if (m_dateChangedListener != null && !m_dateChangedListener.equals(listener)) {
      LOG.error("already a listener registered!");
    }
    else {
      m_dateChangedListener = listener;
    }
  }

  /**
   * aware you must know the registered listener to remove it.
   * 
   * @param listener
   *          the listener to remove.
   * @throws ListenerRegisterException
   *           will be thrown when the passed listener does not match to the
   *           registered listener.
   */
  public void removeTimeChangedListener(AbstractDateSelectionListener listener) {
    if (m_dateChangedListener != listener) {
      LOG.error("no authority to remove the listener!");
    }
    else {
      m_dateChangedListener = null;
    }
  }

  private void fireTimeSelected() {
    DateSelectionEvent e = new DateSelectionEvent(m_cellDate);
    if (m_dateChangedListener != null) {
      m_dateChangedListener.dateChanged(e);
    }
  }

  private class P_MouseOverListener extends MouseTrackAdapter {
    @Override
    public void mouseEnter(MouseEvent e) {
      setStyle(true);
    }

    @Override
    public void mouseExit(MouseEvent e) {
      setStyle(false);
    }
  }

  private class P_TimeSelectionListener extends MouseAdapter {
    @Override
    public void mouseUp(MouseEvent e) {
      fireTimeSelected();
    }
  }

  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }
}
