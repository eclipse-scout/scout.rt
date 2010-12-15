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

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EventListener;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.rt.ui.swing.SingleLayout;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JScrollPaneEx;
import org.eclipse.scout.rt.ui.swing.ext.JTableEx;

/**
 * New analog/digital clock version of the time chooser widget.
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
    m_table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
      private static final long serialVersionUID = 1L;

      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String text = m_timeFormat.format((Date) value);
        setHorizontalAlignment(SwingConstants.CENTER);
        return super.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);
      }
    });
    m_table.setModel(new DefaultTableModel(createTableData(), new Object[]{"Time"}));
    //fire change when selection was chosen
    m_table.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
          if (e.getClickCount() == 1) {
            int row = m_table.rowAtPoint(e.getPoint());
            if (row >= 0 && !m_table.isRowSelected(row)) {
              m_table.setRowSelectionInterval(row, row);
            }
          }
        }
      }
    });
    m_table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        if (m_table.getSelectedRowCount() > 0) {
          fireChangedEvent();
        }
      }
    });
    //container
    JScrollPaneEx sp = new JScrollPaneEx(m_table);
    m_container = new JPanelEx(new SingleLayout());
    m_container.add(sp);
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
      m_table.clearSelection();
      return;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(value);
    int row = (cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)) / 30;
    m_table.setRowSelectionInterval(row, row);
    m_table.scrollRectToVisible(m_table.getCellRect(row, 0, true));
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
}
