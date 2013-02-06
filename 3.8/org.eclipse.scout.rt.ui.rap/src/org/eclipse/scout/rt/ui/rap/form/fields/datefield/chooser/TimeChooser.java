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
package org.eclipse.scout.rt.ui.rap.form.fields.datefield.chooser;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EventListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.rwt.RWT;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Tabular listing of times of every half hour
 */
public class TimeChooser {
  private static final int TABLE_HEIGHT = 200;
  private static final int TABLE_CELL_HEIGHT = SWT.DEFAULT;

  private final EventListenerList m_listenerList = new EventListenerList();

  private Composite m_container;
  private Table m_table;
  private DateFormat m_timeFormat;

  public TimeChooser(Composite parent) {
    m_container = parent;
    m_timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, LocaleThreadLocal.get());

    m_table = new Table(m_container, SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);
    m_table.setLinesVisible(true);
    m_table.setHeaderVisible(false);

    if (getTableCellHeight() != SWT.DEFAULT) {
      m_table.setData(RWT.CUSTOM_ITEM_HEIGHT, getTableCellHeight());
    }

    GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
    if (getTableHeight() != SWT.DEFAULT) {
      data.heightHint = getTableHeight();
    }
    m_table.setLayoutData(data);

    TableColumn column = new TableColumn(m_table, SWT.NONE);
    column.setText("");
    for (Date value : createTableData()) {
      TableItem item = new TableItem(m_table, SWT.NONE);
      item.setData(value);
      if (value != null) {
        item.setText(0, m_timeFormat.format((Date) value));
      }
      else {
        item.setText(0, "n/a");
      }
    }
    m_table.getColumn(0).pack();

    //fire change when selection was chosen
    m_table.addSelectionListener(new SelectionAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetSelected(SelectionEvent e) {
        fireChangedEvent();
      }
    });

    //initial value
    setTime(new Date());
  }

  protected int getTableHeight() {
    return TABLE_HEIGHT;
  }

  protected int getTableCellHeight() {
    return TABLE_CELL_HEIGHT;
  }

  /**
   * fires whenever a date is picked, even if it is the current selection
   */
  public void addChangeListener(ChangeListener listener) {
    m_listenerList.add(ChangeListener.class, listener);
  }

  public void removeChangeListener(ChangeListener listener) {
    m_listenerList.remove(ChangeListener.class, listener);
  }

  /**
   * force changed event
   */
  public void doChanged() {
    fireChangedEvent();
  }

  private void fireChangedEvent() {
    EventListener[] a = m_listenerList.getListeners(ChangeListener.class);
    if (a != null && a.length > 0) {
      ChangeEvent e = new ChangeEvent(this);
      for (int i = 0; i < a.length; i++) {
        ((ChangeListener) a[i]).stateChanged(e);
      }
    }
  }

  public Date getTime() {
    int row = m_table.getSelectionIndex();
    Object selection = (row >= 0 ? m_table.getSelection()[0] : null);
    if (selection instanceof TableItem) {
      return (Date) ((TableItem) selection).getData();
    }
    return null;
  }

  public void setTime(Date value) {
    if (value == null) {
      value = new Date();
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(value);
    int row = (cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)) / 30;
    m_table.setSelection(row);
    m_table.getDisplay().asyncExec(new Runnable() {

      @Override
      public void run() {
        if (m_table == null || m_table.isDisposed()) {
          return;
        }

        m_table.showSelection();
      }
    });
  }

  public Double getTimeAsDouble() {
    Date t = getTime();
    if (t == null) {
      return null;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(getTime());
    return 1.0 * (cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)) / (60.0 * 24.0);
  }

  public void setTimeAsAsDouble(Double d) {
    if (d == null) {
      setTime(null);
      return;
    }
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.add(Calendar.MINUTE, (int) (d.doubleValue() * 24.0 * 60.0 + 0.5));
    setTime(cal.getTime());
  }

  protected Date[] createTableData() {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    Date[] data = new Date[48];
    for (int i = 0; i < data.length; i++) {
      data[i] = cal.getTime();
      cal.add(Calendar.MINUTE, 30);
    }
    return data;
  }
}
