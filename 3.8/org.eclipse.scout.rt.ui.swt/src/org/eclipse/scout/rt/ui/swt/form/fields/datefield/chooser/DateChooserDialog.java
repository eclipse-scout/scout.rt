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

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.ext.table.util.TableCellRolloverSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class DateChooserDialog extends Dialog {

  public static final int TYPE_BACK_YEAR = 1 << 0;
  public static final int TYPE_BACK_MONTH = 1 << 1;
  public static final int TYPE_FOREWARD_MONTH = 1 << 2;
  public static final int TYPE_FOREWARD_YEAR = 1 << 3;

  private static final int COLUMN_WIDTH = 35;
  private TableViewer m_viewer;
  private TableCursor m_cursor;
  private Label m_monthLabel;
  private DatefieldTableModel m_model;
  private Date m_returnDate = null;
  private final ISwtEnvironment m_environment;

  @Override
  protected int getShellStyle() {
    return SWT.NONE;
  }

  public DateChooserDialog(Shell parentShell, Date date, ISwtEnvironment environment) {
    super(parentShell);
    m_environment = environment;
    m_model = new DatefieldTableModel(getEnvironment());
    setDisplayDate(date);
    setBlockOnOpen(true);
  }

  private void setDisplayDate(Date date) {
    m_model.setHighLightDate(date);
    m_model.setNavigationDate(date);
  }

  public Date openDateChooser(Control c) {
    showDialogFor(c);
    return m_returnDate;
  }

  /**
   * @see ch.post.pf.gui.ocp.wt.ext.dialogs.OcpDialog#showDialogFor(org.eclipse.swt.widgets.Control)
   */
  public int showDialogFor(Control field) {
    create();
    getShell().addShellListener(new ShellAdapter() {
      @Override
      public void shellDeactivated(ShellEvent e) {
        close();
      }
    });
    // make sure that the popup fit into the application window.
    Rectangle appBounds = getEnvironment().getDisplay().getBounds();
    Point absPrefPos = field.toDisplay(field.getSize().x - getShell().getSize().x, field.getSize().y);
    Rectangle prefBounds = new Rectangle(absPrefPos.x, absPrefPos.y, getShell().getSize().x, getShell().getSize().y);
    // horizontal correction
    if (prefBounds.x + prefBounds.width > appBounds.width) {
      prefBounds.x = appBounds.width - prefBounds.width;
    }
    // vertical correciton
    if (prefBounds.y + prefBounds.height > appBounds.height) {
      prefBounds.y = appBounds.height - prefBounds.height;
    }
    getShell().setLocation(prefBounds.x, prefBounds.y);
    m_viewer.refresh();
    int ret = this.open();
    return ret;
  }

  @Override
  protected Control createContents(Composite parent) {
    Composite rootArea = new Composite(parent, SWT.NONE);
    Control navigationArea = createControlArea(rootArea);

    Control calendarArea = createPickDateArea(rootArea);
    // layout
    rootArea.setLayout(new FormLayout());
    FormData data = new FormData();
    data.top = new FormAttachment(0, 0);
    data.left = new FormAttachment(0, 0);
    data.right = new FormAttachment(100, 0);
    navigationArea.setLayoutData(data);
    data = new FormData();
    data.top = new FormAttachment(navigationArea, 2);
    data.left = new FormAttachment(0, 0);
    data.right = new FormAttachment(100, 0);
    data.bottom = new FormAttachment(100, 0);
    calendarArea.setLayoutData(data);
    return rootArea;
  }

  private Control createPickDateArea(Composite parent) {
    Table table = getEnvironment().getFormToolkit().createTable(parent, SWT.FULL_SELECTION);
    table.setLinesVisible(true);
    TableViewer viewer = new TableViewer(table);
    new TableCellRolloverSupport(viewer);
    table.setHeaderVisible(true);
    m_cursor = new TableCursor(table, SWT.NONE);

    m_cursor.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseUp(MouseEvent e) {
        TableCursor cursor = (TableCursor) e.widget;
        Date date = ((DateRow) cursor.getRow().getData()).getDate(cursor.getColumn() - 1);
        m_returnDate = date;
        getShell().getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            close();
          }
        });
      }
    });

    TableColumn dummyColumn = new TableColumn(table, SWT.RIGHT);
    dummyColumn.setWidth(0);
    dummyColumn.setResizable(false);
    dummyColumn.setMoveable(false);

    String[] wd = new DateFormatSymbols(Locale.getDefault()).getShortWeekdays();
    // create the m_columns from monday to saturday
    for (int i = 2; i < 8; i++) {
      TableColumn col = new TableColumn(table, SWT.CENTER);
      col.setWidth(COLUMN_WIDTH);
      col.setResizable(false);
      col.setMoveable(false);
      col.setText(wd[i]);
    }
    // sunday
    TableColumn col = new TableColumn(table, SWT.CENTER);
    col.setWidth(COLUMN_WIDTH);
    col.setResizable(false);
    col.setMoveable(false);
    col.setText(wd[Calendar.SUNDAY]);
    // viewer
    m_viewer = viewer;

    m_viewer.setLabelProvider(m_model);
    m_viewer.setContentProvider(m_model);
    m_viewer.setInput(m_model);

    return table;
  }

  private Control createControlArea(Composite parent) {
    Composite rootArea = getEnvironment().getFormToolkit().createComposite(parent, SWT.NONE);
    createButton(rootArea, TYPE_BACK_YEAR);
    createButton(rootArea, TYPE_BACK_MONTH);
    m_monthLabel = getEnvironment().getFormToolkit().createLabel(rootArea, "", SWT.CENTER);
    m_monthLabel.setText(m_model.getMonthYearLabel());
    createButton(rootArea, TYPE_FOREWARD_MONTH);
    createButton(rootArea, TYPE_FOREWARD_YEAR);
    // layout
    rootArea.setLayout(new GridLayout(5, false));
    GridData data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = GridData.FILL;
    m_monthLabel.setLayoutData(data);
    return rootArea;
  }

  private Button createButton(Composite parent, int type) {
    String text = null;
    switch (type) {
      case TYPE_BACK_YEAR:
        text = "<<";
        break;
      case TYPE_BACK_MONTH:
        text = "<";
        break;
      case TYPE_FOREWARD_MONTH:
        text = ">";
        break;
      case TYPE_FOREWARD_YEAR:
        text = ">>";
        break;
    }
    Button b = getEnvironment().getFormToolkit().createButton(parent, text, SWT.PUSH);
    b.addSelectionListener(new P_NavigationSelectionListener(type));
    GridData data = new GridData(22, 22);
    data.horizontalAlignment = GridData.FILL;
    data.grabExcessHorizontalSpace = false;
    b.setLayoutData(data);
    return b;
  }

  private class P_NavigationSelectionListener extends SelectionAdapter {
    private int m_type;

    P_NavigationSelectionListener(int type) {
      m_type = type;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
      switch (m_type) {
        case TYPE_BACK_YEAR:
          m_model.setNavigationDate(DateUtility.addYears(m_model.getNavigationDate(), -1));
          break;
        case TYPE_BACK_MONTH:
          m_model.setNavigationDate(DateUtility.addMonths(m_model.getNavigationDate(), -1));
          break;
        case TYPE_FOREWARD_MONTH:
          m_model.setNavigationDate(DateUtility.addMonths(m_model.getNavigationDate(), 1));
          break;
        case TYPE_FOREWARD_YEAR:
          m_model.setNavigationDate(DateUtility.addYears(m_model.getNavigationDate(), 1));
          break;
      }
      m_viewer.refresh();
      m_monthLabel.setText(m_model.getMonthYearLabel());
    }
  } // end class P_NavigationSelectionListener

  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }
}
