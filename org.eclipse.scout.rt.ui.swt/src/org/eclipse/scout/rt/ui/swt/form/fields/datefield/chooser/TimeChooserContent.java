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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Class UiTimeChooserDialogContent. This is the dialog content of a time
 * chooser, providing a pane to choose a day time. The
 * UiTimeChooserDialogContent is used of the UiDateField.
 * <hr>
 * This component is not yet thought to reuse. It must first be made independent.
 * <hr>
 */
public class TimeChooserContent extends Composite {
  private List<AbstractDateSelectionListener> m_eventListenerList = new ArrayList<AbstractDateSelectionListener>();

  private Composite m_rootPane;

  private List<TimeCell> m_timeCells = new ArrayList<TimeCell>();

  private Date m_date = null;

  private AbstractDateSelectionListener m_timeChangedListener = new P_TimeChangedListener();

  private TimeCell m_selectedCell;

  private final ISwtEnvironment m_environment;

  public TimeChooserContent(Composite parent, Date date, ISwtEnvironment environment) {
    super(parent, SWT.NONE);
    m_date = date;
    m_environment = environment;
    createDialogContent(this);
    setLayout(new FillLayout());
  }

  /**
   * @see ch.post.pf.gui.ocp.wt.ext.definitions.OcpWindowContent#createDialogContent(org.eclipse.swt.widgets.Composite)
   */
  public void createDialogContent(Composite parent) {
    m_rootPane = new Composite(parent, SWT.NONE);
    Composite timeCellBag = createCalendarCells(m_rootPane);
    // layouting
    FormLayout rootLayout = new FormLayout();
    m_rootPane.setLayout(rootLayout);
    FormData cellBagData = new FormData(SWT.DEFAULT, SWT.DEFAULT);
    cellBagData.right = new FormAttachment(100, 0);
    cellBagData.left = new FormAttachment(0, 0);
    cellBagData.top = new FormAttachment(0, 0);
    timeCellBag.setLayoutData(cellBagData);
    setDate(m_date);
  }

  private Composite createCalendarCells(Composite parent) {
    Composite timeCellBag = new Composite(parent, SWT.NONE);
    GridLayout timeCellBagLayout = new GridLayout(4, false);
    timeCellBagLayout.horizontalSpacing = 1;
    timeCellBagLayout.verticalSpacing = 1;
    timeCellBagLayout.marginHeight = 0;
    timeCellBagLayout.marginWidth = 0;
    timeCellBag.setLayout(timeCellBagLayout);
    for (int i = 0; i < 96; i++) {
      TimeCell cell = new TimeCell(timeCellBag, m_timeChangedListener, getEnvironment());
      // Layoutdata is set in the SwtTimeCell class but it should be a widget
      m_timeCells.add(cell);
    }
    return timeCellBag;
  }

  private void updateStates() {
    Calendar c = Calendar.getInstance();
    c.setTime(m_date);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    for (TimeCell cell : m_timeCells) {
      DateEquality dateEquality = compareDates(c.getTime());
      cell.setRepresentedState(c, dateEquality);
      if (dateEquality.equals(DateEquality.QUARTER_EQUAL)) {
        m_selectedCell = cell;
      }
      c.add(Calendar.MINUTE, 15);
    }
  }

  /**
   * @return teh selected time cell
   */
  public TimeCell getSelectedCell() {
    return m_selectedCell;
  }

  private DateEquality compareDates(Date d) {
    Calendar c = Calendar.getInstance();
    c.setTime(m_date);
    int selHour = c.get(Calendar.HOUR_OF_DAY);
    int selMin = round(c.get(Calendar.MINUTE));
    c.setTime(d);
    int cellHour = c.get(Calendar.HOUR_OF_DAY);
    int cellMin = c.get(Calendar.MINUTE);
    if (selHour == cellHour) {
      if (selMin == cellMin) {
        return DateEquality.QUARTER_EQUAL;
      }
      return DateEquality.HOUR_EQUAL;
    }
    return DateEquality.NOT_EQUAL;
  }

  private int round(int i) {
    int diff = i % 15;
    return i - diff;
  }

  /**
   * to set the daytime, this cell will be highlighted
   * 
   * @param d
   *          *
   */
  public void setDate(Date d) {
    if (d == null) {
      m_date = new Date();
    }
    else {
      m_date = d;
    }
    updateStates();
  }

  /**
   * set day time using a float from 0...1
   * 
   * @param f
   *          *
   */
  public void setTime(float f) {
    Calendar c = Calendar.getInstance();
    int minutes = (int) (f * 24 * 60);
    int h = minutes / 60;
    int m = minutes % 60;
    c.set(Calendar.HOUR_OF_DAY, h);
    c.set(Calendar.MINUTE, m);
    m_date = c.getTime();
    updateStates();
  }

  /**
   * @param listener
   *          the listener will be notified by selection changes
   */
  public void addTimeChangedListener(AbstractDateSelectionListener listener) {
    m_eventListenerList.add(listener);
  }

  /**
   * @param listener
   *          the date selection listener to remove
   */
  public void removeTimeChangedListener(AbstractDateSelectionListener listener) {
    m_eventListenerList.remove(listener);
  }

  private void fireTimeSelected(DateSelectionEvent e) {
    for (AbstractDateSelectionListener listener : m_eventListenerList) {
      listener.dateChanged(e);
    }
  }

  private class P_TimeChangedListener extends AbstractDateSelectionListener {
    @Override
    public void dateChanged(DateSelectionEvent e) {
      setDate((Date) e.getData());
      fireTimeSelected(e);
    }
  }

  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }
}
