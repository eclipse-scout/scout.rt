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
package org.eclipse.scout.rt.ui.swt.basic.table.celleditor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorDeactivationEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableUtility;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.ui.swt.basic.ISwtScoutComposite;
import org.eclipse.scout.rt.ui.swt.basic.table.ISwtScoutTable;
import org.eclipse.scout.rt.ui.swt.basic.table.SwtScoutTable;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.swt.form.fields.IPopupSupport;
import org.eclipse.scout.rt.ui.swt.form.fields.IPopupSupport.IPopupSupportListener;
import org.eclipse.scout.rt.ui.swt.keystroke.SwtKeyStroke;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Widget;

/**
 * <h3>SwtScoutTableCellEditor</h3> ...
 * 
 * @author imo
 * @since 1.0.8 30.06.2010
 */
public class SwtScoutTableCellEditor {
  private static final String DUMMY_VALUE = "Dummy";

  private final ISwtScoutTable m_tableComposite;
  private final Listener m_rowHeightListener;

  private P_FocusLostListener m_focusLostListener;
  private Object m_focusLostListenerLock;

  public SwtScoutTableCellEditor(final ISwtScoutTable tableComposite) {
    m_focusLostListenerLock = new Object();
    m_tableComposite = tableComposite;
    m_rowHeightListener = new Listener() {
      @Override
      public void handleEvent(Event event) {
        event.height = Math.max(event.height, UiDecorationExtensionPoint.getLookAndFeel().getLogicalGridLayoutRowHeight());
      }
    };
  }

  //(re)install cell editors
  public void initialize() {
    TableViewer viewer = m_tableComposite.getSwtTableViewer();
    String[] columnPropertyNames = new String[viewer.getTable().getColumnCount()];
    CellEditor[] oldEditors = viewer.getCellEditors();
    CellEditor[] newEditors = new CellEditor[columnPropertyNames.length];
    boolean hasEditors = false;
    for (int i = 0; i < columnPropertyNames.length; i++) {
      TableColumn swtCol = viewer.getTable().getColumn(i);
      IColumn<?> scoutCol = (IColumn<?>) swtCol.getData(SwtScoutTable.KEY_SCOUT_COLUMN);
      if (scoutCol != null) {
        columnPropertyNames[i] = "" + scoutCol.getColumnIndex();
        if (scoutCol.isEditable()) {
          hasEditors = true;
          newEditors[i] = new P_SwtCellEditor(viewer.getTable());
        }
      }
      else {
        columnPropertyNames[i] = "";
      }
    }
    viewer.setCellModifier(new P_SwtCellModifier());
    viewer.setColumnProperties(columnPropertyNames);
    viewer.setCellEditors(newEditors);
    if (oldEditors != null && oldEditors.length > 0) {
      for (CellEditor editor : oldEditors) {
        if (editor != null) {
          editor.dispose();
        }
      }
    }
    //increase row height when editors are present
    if (hasEditors) {
      viewer.getTable().addListener(SWT.MeasureItem, m_rowHeightListener);
    }
    else {
      viewer.getTable().removeListener(SWT.MeasureItem, m_rowHeightListener);
    }
  }

  /**
   * Installs a listener to close all active cell editors if the table looses the focus
   */
  private void installFocusLostListener() {
    synchronized (m_focusLostListenerLock) {
      if (m_focusLostListener == null) {
        m_focusLostListener = new P_FocusLostListener();
        m_tableComposite.getEnvironment().getDisplay().addFilter(SWT.FocusIn, m_focusLostListener);
      }
    }

    // ensure cell editors only to be active if focus is on table
    Event event = new Event();
    event.widget = m_tableComposite.getEnvironment().getDisplay().getFocusControl();
    m_focusLostListener.handleEvent(event);
  }

  private void uninstallFocusLostListener() {
    synchronized (m_focusLostListenerLock) {
      if (m_focusLostListener != null) {
        m_tableComposite.getEnvironment().getDisplay().removeFilter(SWT.FocusIn, m_focusLostListener);
        m_focusLostListener = null;
      }
    }
  }

  protected Control getEditorControl(Composite parent, ITableRow scoutRow, IColumn<?> scoutCol) {
    //no caching
    Control swtEditorControl = null;
    ISwtScoutComposite<? extends IFormField> editorComposite = createEditorComposite(parent, scoutRow, scoutCol);
    if (editorComposite != null) {
      decorateEditorComposite(editorComposite, scoutRow, scoutCol);
      swtEditorControl = editorComposite.getSwtContainer();
    }
    return swtEditorControl;
  }

  @SuppressWarnings("unchecked")
  protected ISwtScoutComposite<? extends IFormField> createEditorComposite(Composite parent, final ITableRow scoutRow, final IColumn<?> scoutCol) {
    final AtomicReference<IFormField> fieldRef = new AtomicReference<IFormField>();
    if (scoutRow != null && scoutCol != null) {
      Runnable t = new Runnable() {
        @Override
        public void run() {
          fieldRef.set(m_tableComposite.getScoutObject().getUIFacade().prepareCellEditFromUI(scoutRow, scoutCol));
          synchronized (fieldRef) {
            fieldRef.notifyAll();
          }
        }
      };
      synchronized (fieldRef) {
        m_tableComposite.getEnvironment().invokeScoutLater(t, 2345);
        try {
          fieldRef.wait(2345);
        }
        catch (InterruptedException e) {
          //nop
        }
      }
    }
    IFormField formField = fieldRef.get();
    if (formField == null) {
      return null;
    }

    ISwtScoutComposite swtScoutFormField;
    if (formField instanceof IStringField && ((IStringField) formField).isMultilineText()) {
      // for fields to be presented as popup dialog
      swtScoutFormField = createEditorCompositesPopup(parent, formField, scoutRow, scoutCol);
    }
    else {
      swtScoutFormField = m_tableComposite.getEnvironment().createFormField(parent, formField);
    }

    // If the SWT field uses a @{Shell} to edit its value, the focus on the table gets lost while the shell is open.
    // To prevent the cell editor from being closed, the focus lost listener must be uninstalled for the time the shell is open.
    if (swtScoutFormField instanceof IPopupSupport) {
      ((IPopupSupport) swtScoutFormField).addPopupEventListener(new IPopupSupportListener() {

        @Override
        public void handleEvent(int eventType) {
          if (eventType == IPopupSupportListener.TYPE_OPENING) {
            uninstallFocusLostListener();
          }
          else if (eventType == IPopupSupportListener.TYPE_CLOSED) {
            installFocusLostListener();
          }
        }
      });
    }
    return swtScoutFormField;
  }

  protected ISwtScoutComposite<? extends IFormField> createEditorCompositesPopup(Composite parent, IFormField formField, final ITableRow scoutRow, final IColumn<?> scoutCol) {
    // uninstall focus lost listener as new shell is used for popup
    uninstallFocusLostListener();

    // overwrite layout properties
    GridData gd = formField.getGridData();
    gd.h = 1;
    gd.w = IFormField.FULL_WIDTH;
    gd.weightY = 1;
    gd.weightX = 1;
    formField.setGridDataInternal(gd);

    TableColumn swtCol = getSwtColumn(scoutCol);
    final P_SwtCellEditor cellEditor = (P_SwtCellEditor) m_tableComposite.getSwtTableViewer().getCellEditors()[getSwtColumnIndex(swtCol)];

    int prefWidth = gd.widthInPixel;
    int minWidth = swtCol.getWidth();
    int prefHeight = gd.heightInPixel;
    int minHeight = Math.max(105, m_tableComposite.getSwtTableViewer().getTable().getItemHeight());

    prefHeight = Math.max(prefHeight, minHeight);
    prefWidth = Math.max(prefWidth, minWidth);

    // create placeholder field to represent the cell editor
    Composite cellEditorComposite = new Composite(parent, SWT.NONE);

    // create popup dialog to wrap the form field
    SwtScoutFormFieldPopup formFieldDialog = new SwtScoutFormFieldPopup(cellEditorComposite);
    formFieldDialog.setPrefHeight(prefHeight);
    formFieldDialog.setPrefWidth(prefWidth);
    formFieldDialog.setMinHeight(minHeight);
    formFieldDialog.setMinWidth(minWidth);
    formFieldDialog.createField(parent, formField, m_tableComposite.getEnvironment());
    formFieldDialog.addEventListener(new IFormFieldPopupEventListener() {

      @Override
      public void handleEvent(FormFieldPopupEvent event) {
        // install focus lost listener as shell is closed
        installFocusLostListener();

        if ((event.getType() & FormFieldPopupEvent.TYPE_OK) > 0) {
          // save cell editor
          cellEditor.stopCellEditing();
        }
        else if ((event.getType() & FormFieldPopupEvent.TYPE_CANCEL) > 0) {
          // cancel cell editor
          cellEditor.cancelCellEditing();
        }

        // traversal control
        if ((event.getType() & FormFieldPopupEvent.TYPE_FOCUS_BACK) > 0) {
          enqueueEditNextTableCell(scoutRow, scoutCol, false);
        }
        else if ((event.getType() & FormFieldPopupEvent.TYPE_FOCUS_NEXT) > 0) {
          enqueueEditNextTableCell(scoutRow, scoutCol, true);
        }
      }
    });

    return formFieldDialog;
  }

  private TableColumn getSwtColumn(IColumn<?> scoutCol) {
    for (TableColumn swtCol : m_tableComposite.getSwtTableViewer().getTable().getColumns()) {
      IColumn<?> candidate = (IColumn<?>) swtCol.getData(SwtScoutTable.KEY_SCOUT_COLUMN);
      if (candidate != null && CompareUtility.equals(candidate.getColumnId(), scoutCol.getColumnId())) {
        return swtCol;
      }
    }
    return null;
  }

  private int getSwtColumnIndex(TableColumn swtCol) {
    Table table = m_tableComposite.getSwtTableViewer().getTable();
    for (int i = 0; i < table.getColumnCount(); i++) {
      if (table.getColumn(i) == swtCol) {
        return i;
      }
    }
    return -1;
  }

  protected void decorateEditorComposite(ISwtScoutComposite<? extends IFormField> editorComposite, final ITableRow scoutRow, final IColumn<?> scoutCol) {
  }

  protected void saveEditorFromSwt() {
    Runnable t = new Runnable() {
      @Override
      public void run() {
        m_tableComposite.getScoutObject().getUIFacade().completeCellEditFromUI();
      }
    };
    m_tableComposite.getEnvironment().invokeScoutLater(t, 0);
  }

  protected void cancelEditorFromSwt() {
    Runnable t = new Runnable() {
      @Override
      public void run() {
        m_tableComposite.getScoutObject().getUIFacade().cancelCellEditFromUI();
      }
    };
    m_tableComposite.getEnvironment().invokeScoutLater(t, 0);
  }

  protected void enqueueEditNextTableCell(final ITableRow row, final IColumn<?> col, final boolean forward) {
    if (row == null || col == null) {
      return;
    }
    m_tableComposite.getEnvironment().invokeScoutLater(new Runnable() {
      @Override
      public void run() {
        if (m_tableComposite.getEnvironment() == null) {
          return;
        }
        ITable table = m_tableComposite.getScoutObject();
        TableUtility.editNextTableCell(table, row, col, forward, new TableUtility.ITableCellEditorFilter() {
          @Override
          public boolean accept(ITableRow rowx, IColumn<?> colx) {
            return !(colx instanceof IBooleanColumn);
          }
        });
      }
    }, 0L);
  }

  protected IColumn<?> getScoutColumn(String property) {
    if (property != null && property.matches("[0-9]+")) {
      int colIndex = Integer.parseInt(property);
      return m_tableComposite.getScoutObject().getColumnSet().getColumn(colIndex);
    }
    return null;
  }

  private class P_SwtCellModifier implements ICellModifier {

    @Override
    public void modify(Object element, String property, Object value) {
      saveEditorFromSwt();
    }

    @Override
    public Object getValue(Object element, String property) {
      //not used
      return DUMMY_VALUE;
    }

    @Override
    public boolean canModify(Object element, String property) {
      final ITable table = m_tableComposite.getScoutObject();
      final ITableRow row = (ITableRow) element;
      final IColumn<?> column = getScoutColumn(property);
      //make a safe model call
      final AtomicBoolean b = new AtomicBoolean();
      synchronized (b) {
        Runnable r = new Runnable() {
          @Override
          public void run() {
            // try first
            synchronized (b) {
              try {
                if (table != null && row != null && column != null) {
                  if (column instanceof IBooleanColumn) {
                    b.set(false);
                  }
                  else {
                    b.set(table.isCellEditable(row, column));
                  }
                }
              }
              catch (Throwable ex) {
                //fast access: ignore
              }
              b.notifyAll();
            }
          }
        };
        m_tableComposite.getEnvironment().invokeScoutLater(r, 2345);
        try {
          b.wait(2345);
        }
        catch (InterruptedException e) {
          //nop
        }
      }
      return b.get();
    }
  }

  private class P_SwtCellEditor extends CellEditor {
    private Composite m_container;
    private Object m_value;
    private ITableRow m_editScoutRow;
    private IColumn<?> m_editScoutCol;

    protected P_SwtCellEditor(Composite parent) {
      super(parent);
    }

    @Override
    protected Control createControl(Composite parent) {
      m_container = new Composite(parent, SWT.NONE) {
        /*
         * disable inner components preferred sizes
         */
        @Override
        public Point computeSize(int wHint, int hHint, boolean changed) {
          return new Point(wHint, hHint);
        }
      };
      m_container.setLayout(new FillLayout());
      m_tableComposite.getEnvironment().addKeyStroke(m_container, new SwtKeyStroke(SWT.ESC) {
        @Override
        public void handleSwtAction(Event e) {
          e.doit = false;
          fireCancelEditor();
        }
      });
      m_tableComposite.getEnvironment().addKeyStroke(m_container, new SwtKeyStroke(SWT.CR) {
        @Override
        public void handleSwtAction(Event e) {
          e.doit = false;
          fireApplyEditorValue();
          deactivate();
        }
      });
      return m_container;
    }

    @Override
    protected void doSetFocus() {
      m_container.traverse(SWT.TRAVERSE_TAB_NEXT);
      Control focusControl = m_container.getDisplay().getFocusControl();
      if (focusControl != null && !SwtUtility.isAncestorOf(m_container, focusControl)) {
        focusControl = null;
      }
      if (focusControl != null) {
        focusControl.addTraverseListener(new TraverseListener() {
          @Override
          public void keyTraversed(TraverseEvent e) {
            switch (e.detail) {
              case SWT.TRAVERSE_ESCAPE:
              case SWT.TRAVERSE_RETURN: {
              e.doit = false;
              break;
            }
            case SWT.TRAVERSE_TAB_NEXT: {
              e.doit = false;
              ITableRow scoutRow = m_editScoutRow;
              IColumn<?> scoutCol = m_editScoutCol;
              fireApplyEditorValue();
              deactivate();
              enqueueEditNextTableCell(scoutRow, scoutCol, true);
              break;
            }
            case SWT.TRAVERSE_TAB_PREVIOUS: {
              e.doit = false;
              ITableRow scoutRow = m_editScoutRow;
              IColumn<?> scoutCol = m_editScoutCol;
              fireApplyEditorValue();
              deactivate();
              enqueueEditNextTableCell(scoutRow, scoutCol, false);
              break;
            }
          }
        }
        });
      }
    }

    @Override
    protected Object doGetValue() {
      return m_value;
    }

    @Override
    protected void doSetValue(Object value) {
      m_value = value;
    }

    @Override
    public void activate(ColumnViewerEditorActivationEvent e) {
      installFocusLostListener();

      m_editScoutRow = null;
      m_editScoutCol = null;
      if (e.getSource() instanceof ViewerCell) {
        ViewerCell cell = (ViewerCell) e.getSource();
        TableViewer viewer = m_tableComposite.getSwtTableViewer();
        TableColumn swtCol = viewer.getTable().getColumn(cell.getColumnIndex());
        IColumn<?> scoutCol = (IColumn<?>) swtCol.getData(SwtScoutTable.KEY_SCOUT_COLUMN);
        ITableRow scoutRow = (ITableRow) cell.getElement();
        //no edit on boolean column when mouse was clicked
        if (e.sourceEvent instanceof MouseEvent) {
          if (scoutCol instanceof IBooleanColumn) {
            return;
          }
        }
        if (scoutRow != null && scoutCol != null) {
          m_editScoutRow = scoutRow;
          m_editScoutCol = scoutCol;
          @SuppressWarnings("unused")
          Control control = getEditorControl(m_container, scoutRow, scoutCol);
        }
        m_container.layout(true, true);
        m_container.setVisible(true);
      }
    }

    @Override
    protected void deactivate(ColumnViewerEditorDeactivationEvent e) {
      uninstallFocusLostListener();

      m_editScoutRow = null;
      m_editScoutCol = null;
      for (Control c : m_container.getChildren()) {
        c.dispose();
      }
      super.deactivate(e);
      if (e.eventType == ColumnViewerEditorDeactivationEvent.EDITOR_CANCELED) {
        cancelEditorFromSwt();
      }
    }

    public void stopCellEditing() {
      fireApplyEditorValue();
      deactivate();
    }

    public void cancelCellEditing() {
      fireCancelEditor();
      deactivate();
    }
  }

  /**
   * Hysteresis listener that commits the cell editor when the table has first received focus and then lost it. That is
   * because cell editors in SWT are not closed automatically if the table looses the focus.
   */
  private class P_FocusLostListener implements Listener {

    @Override
    public void handleEvent(Event event) {
      Widget w = event.widget;
      if (w == null || !(w instanceof Control) || w.isDisposed()) {
        return;
      }
      TableViewer viewer = m_tableComposite.getSwtTableViewer();
      if (!viewer.isCellEditorActive()) {
        return;
      }

      Control candidate = (Control) w;
      Control tableControl = m_tableComposite.getSwtTableViewer().getControl();

      if (!SwtUtility.isAncestorOf(tableControl, candidate)) {
        for (CellEditor editor : viewer.getCellEditors()) {
          if (editor != null && editor.isActivated() && editor instanceof P_SwtCellEditor) {
            ((P_SwtCellEditor) editor).stopCellEditing();
            break;
          }
        }
      }
    }
  }
}
