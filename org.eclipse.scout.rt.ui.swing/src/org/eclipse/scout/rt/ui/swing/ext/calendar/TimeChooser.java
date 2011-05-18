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
package org.eclipse.scout.rt.ui.swing.ext.calendar;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EventListener;

import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.rt.ui.swing.SingleLayout;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JScrollPaneEx;
import org.eclipse.scout.rt.ui.swing.ext.JTableEx;

/**
 * Tabular listing of times of every half hour
 * 
 * @author imo
 */
public class TimeChooser {

  private final EventListenerList m_listenerList = new EventListenerList();
  private JPanelEx m_container;
  private JTableEx m_table;
  private DateFormat m_timeFormat;

  public TimeChooser() {
    m_timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
    //table
    m_table = new JTableEx();
    m_table.setAutoResizeMode(JTableEx.AUTO_RESIZE_ALL_COLUMNS);
    m_table.setTableHeader(null);
    m_table.setModel(new P_TableModel());
    m_table.setDefaultRenderer(Date.class, new DefaultTableCellRenderer() {
      private static final long serialVersionUID = 1L;

      @Override
      protected void setValue(Object value) {
        if (value != null) {
          setText(m_timeFormat.format((Date) value));
        }
        else {
          setText("n/a");
        }
      }
    });

    //fire change when selection was chosen
    m_table.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
          if (m_table.getSelectedRow() >= 0) {
            fireChangedEvent();
          }
        }
      }
    });

    //container
    JScrollPaneEx sp = new JScrollPaneEx(m_table);
    m_container = new JPanelEx(new SingleLayout());
    m_container.add(sp);
    m_container.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        int row = m_table.getSelectedRow();
        if (row >= 0) {
          m_table.scrollRectToVisible(m_table.getCellRect(Math.min(m_table.getRowCount() - 1, row + 6), 0, true));
          m_table.scrollRectToVisible(m_table.getCellRect(Math.max(0, row - 6), 0, true));
        }
      }
    });
    //synth
    m_container.setBackground(UIManager.getColor("Calendar.date.background"));
    m_container.setName("Synth.TimeChooser");
    //initial value
    setTime(new Date());
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

  public JPanel getContainer() {
    return m_container;
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
    int row = m_table.getSelectedRow();
    return (Date) (row >= 0 ? m_table.getValueAt(row, 0) : null);
  }

  public void setTime(Date value) {
    if (value == null) {
      value = new Date();
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(value);
    int row = (cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)) / 30;
    m_table.setRowSelectionInterval(row, row);
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

  protected Object[][] createTableData() {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    Object[][] data = new Object[48][1];
    for (int i = 0; i < data.length; i++) {
      data[i][0] = cal.getTime();
      cal.add(Calendar.MINUTE, 30);
    }
    return data;
  }

  private class P_TableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private Object[][] m_data;

    public P_TableModel() {
      m_data = createTableData();
    }

    @Override
    public int getRowCount() {
      return m_data.length;
    }

    @Override
    public int getColumnCount() {
      return 1;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      return Date.class;
    }

    @Override
    public String getColumnName(int column) {
      return "Time";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      return m_data[rowIndex][columnIndex];
    }
  }
}
