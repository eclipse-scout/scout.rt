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
package org.eclipse.scout.rt.ui.rap.form.fields.datefield.chooser;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.rt.ui.rap.ext.table.util.TableCellRolloverSupport;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class DateChooserDialog extends Dialog {
  private static final long serialVersionUID = 1L;

  private static final String NEXT_YEAR_CUSTOM_VARIANT = "datechooser-dialog-next-year";
  private static final String NEXT_MONTH_CUSTOM_VARIANT = "datechooser-dialog-next-month";
  private static final String LAST_MONTH_CUSTOM_VARIANT = "datechooser-dialog-last-month";
  private static final String LAST_YEAR_CUSTOM_VARIANT = "datechooser-dialog-last-year";
  private static final String DATECHOOSER_DIALOG_CUSTOM_VARIANT = "datechooser-dialog";

  public static final int TYPE_BACK_YEAR = 1 << 0;
  public static final int TYPE_BACK_MONTH = 1 << 1;
  public static final int TYPE_FOREWARD_MONTH = 1 << 2;
  public static final int TYPE_FOREWARD_YEAR = 1 << 3;

  private static final int DATE_CELL_WIDTH = 35;
  private static final int DATE_CELL_HEIGHT = SWT.DEFAULT;
  private static final int CONTROL_BUTTON_WIDTH = 15;
  private static final int CONTROL_BUTTON_HEIGHT = 15;
  private TableViewer m_viewer;
  private Label m_monthLabel;
  private DatefieldTableModel m_model;
  private Date m_returnDate = null;
  private Control m_field;

  @Override
  protected int getShellStyle() {
    return SWT.NONE;
  }

  /**
   * @param field
   *          used to compute initial Location, see {@link #computeInitialLocation(Control)}. May be null.
   */
  public DateChooserDialog(Shell parentShell, Control field, Date date) {
    super(parentShell);
    m_model = new DatefieldTableModel(RwtUtility.getClientSessionLocale(parentShell.getDisplay()));
    m_field = field;
    setDisplayDate(date);
    setBlockOnOpen(false);
    create();
  }

  private void setDisplayDate(Date date) {
    m_model.setHighLightDate(date);
    m_model.setNavigationDate(date);
  }

  public Date getReturnDate() {
    return m_returnDate;
  }

  protected Control getField() {
    return m_field;
  }

  /**
   * Override this method to set a custom location.
   * <p>
   * As default the popup is opened right under the field.
   * </p>
   */
  protected Point getInitialLocation(Point initialSize, Control field) {
    // make sure that the popup fit into the application window.
    Rectangle appBounds = field.getDisplay().getBounds();
    Point absPrefPos = field.toDisplay(0, field.getSize().y);
    Rectangle prefBounds = new Rectangle(absPrefPos.x, absPrefPos.y, initialSize.x, initialSize.y);

    // horizontal correction
    if (prefBounds.x + prefBounds.width > appBounds.width) {
      prefBounds.x = appBounds.width - prefBounds.width;
    }
    // vertical correction
    if (prefBounds.y + prefBounds.height > appBounds.height) {
      if (dialogFitsAboveField(absPrefPos, prefBounds, field)) {
        prefBounds.y = getYCoordinateForDialogAboveField(absPrefPos, prefBounds, field);
      }
      else {
        prefBounds.y = appBounds.height - prefBounds.height;
      }
    }

    return new Point(prefBounds.x, prefBounds.y);
  }

  /**
   * Checks if the dialog does fit above the given field.
   *
   * @since 5.0-M2
   */
  private boolean dialogFitsAboveField(Point absPrefPos, Rectangle dialogPrefBounds, Control field) {
    return getYCoordinateForDialogAboveField(absPrefPos, dialogPrefBounds, field) >= 0;
  }

  /**
   * Returns the Y-Position for the DateChooserDialog if it should be displayed above the given field.
   *
   * @since 5.0-M2
   */
  private int getYCoordinateForDialogAboveField(Point absPrefPos, Rectangle dialogPrefBounds, Control field) {
    return absPrefPos.y - field.getBounds().height - dialogPrefBounds.height;
  }

  @Override
  protected Point getInitialLocation(Point initialSize) {
    Point initialLocation = getInitialLocation(initialSize, getField());
    if (initialLocation != null) {
      return initialLocation;
    }
    return super.getInitialLocation(initialSize);
  }

  @Override
  protected Control createContents(Composite parent) {
    parent.setData(RWT.CUSTOM_VARIANT, getDialogVariant());

    Composite rootArea = new Composite(parent, SWT.NO_FOCUS);
    rootArea.setData(RWT.CUSTOM_VARIANT, getDialogVariant());
    GridData gridData = new GridData(SWT.CENTER, SWT.CENTER, true, true);
    rootArea.setLayoutData(gridData);

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
    data.bottom = new FormAttachment(100, -5);
    calendarArea.setLayoutData(data);

    return rootArea;
  }

  protected String getDialogVariant() {
    return DATECHOOSER_DIALOG_CUSTOM_VARIANT;
  }

  private Control createPickDateArea(Composite parent) {
    final Table table = new Table(parent, SWT.SINGLE | SWT.NO_SCROLL | SWT.HIDE_SELECTION);
    table.setData(RWT.CUSTOM_VARIANT, getDialogVariant());
    if (getDateCellHeight() != SWT.DEFAULT) {
      table.setData(RWT.CUSTOM_ITEM_HEIGHT, getDateCellHeight());
    }
    table.setLinesVisible(true);
    TableViewer viewer = new TableViewer(table);
    new TableCellRolloverSupport(viewer);
    table.setHeaderVisible(true);

    table.addListener(SWT.MouseDown, new Listener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void handleEvent(Event event) {
        switch (event.type) {
          case SWT.MouseDown: {
            TableColumn columnAt = RwtUtility.getRwtColumnAt(table, new Point(event.x, event.y));
            TableItem item = table.getItem(new Point(event.x, event.y));
            if (item != null) {
              item.setData(RWT.CUSTOM_VARIANT, getDialogVariant());
              if (columnAt != null) {
                Date date = ((DateRow) item.getData()).getDate(table.indexOf(columnAt) - 1);
                m_returnDate = date;
                getShell().getDisplay().asyncExec(new Runnable() {
                  @Override
                  public void run() {
                    close();
                  }
                });
              }
            }
            break;
          }
        }
      }
    });

    TableColumn dummyColumn = new TableColumn(table, SWT.RIGHT);
    dummyColumn.setData(RWT.CUSTOM_VARIANT, getDialogVariant());
    dummyColumn.setWidth(0);
    dummyColumn.setResizable(false);
    dummyColumn.setMoveable(false);

    String[] wd = new DateFormatSymbols(RwtUtility.getClientSessionLocale(parent.getDisplay())).getShortWeekdays();
    // create the m_columns from monday to saturday
    for (int i = 2; i < 8; i++) {
      TableColumn col = new TableColumn(table, SWT.CENTER);
      col.setData(RWT.CUSTOM_VARIANT, getDialogVariant());
      col.setWidth(getDateCellWidth());
      col.setResizable(false);
      col.setMoveable(false);
      col.setText(wd[i]);
    }
    // sunday
    TableColumn col = new TableColumn(table, SWT.CENTER);
    col.setData(RWT.CUSTOM_VARIANT, getDialogVariant());
    col.setWidth(getDateCellWidth());
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

  protected int getDateCellWidth() {
    return DATE_CELL_WIDTH;
  }

  protected int getDateCellHeight() {
    return DATE_CELL_HEIGHT;
  }

  private Control createControlArea(Composite parent) {
    Composite rootArea = RwtUtility.getUiEnvironment(parent.getDisplay()).getFormToolkit().createComposite(parent, SWT.NO_FOCUS);
    rootArea.setData(RWT.CUSTOM_VARIANT, getDialogVariant());
    createButton(rootArea, TYPE_BACK_YEAR);
    createButton(rootArea, TYPE_BACK_MONTH);
    m_monthLabel = new Label(rootArea, SWT.CENTER);
    m_monthLabel.setText(m_model.getMonthYearLabel());
    m_monthLabel.setData(RWT.CUSTOM_VARIANT, getDialogVariant());
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
    String variant = getControlButtonVariant(type);

    Button b = new Button(parent, SWT.PUSH);
    b.setData(RWT.CUSTOM_VARIANT, variant);
    b.addMouseListener(new P_NavigationMouseListener(type));
    GridData data = new GridData(getControlButtonWidth(), getControlButtonHeight());
    data.horizontalAlignment = GridData.FILL;
    data.grabExcessHorizontalSpace = false;
    b.setLayoutData(data);
    return b;
  }

  /**
   * Override this method to set a custom variant name for the control buttons
   */
  protected String getControlButtonVariant(int type) {
    switch (type) {
      case TYPE_BACK_YEAR:
        return LAST_YEAR_CUSTOM_VARIANT;
      case TYPE_BACK_MONTH:
        return LAST_MONTH_CUSTOM_VARIANT;
      case TYPE_FOREWARD_MONTH:
        return NEXT_MONTH_CUSTOM_VARIANT;
      case TYPE_FOREWARD_YEAR:
        return NEXT_YEAR_CUSTOM_VARIANT;
      default:
        return null;
    }
  }

  protected int getControlButtonWidth() {
    return CONTROL_BUTTON_WIDTH;
  }

  protected int getControlButtonHeight() {
    return CONTROL_BUTTON_HEIGHT;
  }

  private class P_NavigationMouseListener extends MouseAdapter {
    private static final long serialVersionUID = 1L;
    private int m_type;

    P_NavigationMouseListener(int type) {
      m_type = type;
    }

    @Override
    public void mouseDown(MouseEvent e) {
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
}
