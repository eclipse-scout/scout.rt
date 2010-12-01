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
package org.eclipse.scout.rt.ui.swt.basic.table;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorDeactivationEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.ui.swt.basic.ISwtScoutComposite;
import org.eclipse.scout.rt.ui.swt.ext.table.TableViewerEx;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.swt.keystroke.SwtKeyStroke;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
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
  //cache
  private boolean m_tableIsEditingAndContainsFocus;

  public SwtScoutTableCellEditor(ISwtScoutTable tableComposite) {
    m_tableComposite = tableComposite;
    m_rowHeightListener = new Listener() {
      public void handleEvent(Event event) {
        event.height = UiDecorationExtensionPoint.getLookAndFeel().getLogicalGridLayoutRowHeight();
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
    //add a hysteresis listener that commits the cell editor when the table has first received focus and then lost it
    m_tableComposite.getEnvironment().getDisplay().addFilter(SWT.FocusIn, new Listener() {
      @Override
      public void handleEvent(Event e) {
        Widget c = e.widget;
        if (c == null || !(c instanceof Control)) {
          return;
        }
        boolean oldValue = m_tableIsEditingAndContainsFocus;
        boolean newValue = SwtUtility.isAncestorOf(m_tableComposite.getSwtField(), (Control) c);
        m_tableIsEditingAndContainsFocus = newValue;
        if (oldValue && !newValue) {
          TableViewer v = m_tableComposite.getSwtTableViewer();
          if (v instanceof TableViewerEx) {
            ((TableViewerEx) v).applyEditorValue();
          }
        }
      }
    });
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
    if (fieldRef.get() != null) {
      return m_tableComposite.getEnvironment().createFormField(parent, fieldRef.get());
    }
    else {
      return null;
    }
  }

  protected void decorateEditorComposite(ISwtScoutComposite<? extends IFormField> editorComposite, final ITableRow scoutRow, final IColumn<?> scoutCol) {
    //auto toggle checkboxes
    if (editorComposite.getScoutObject() instanceof IBooleanField) {
      final IBooleanField cb = (IBooleanField) editorComposite.getScoutObject();
      Runnable t = new Runnable() {
        @Override
        public void run() {
          cb.getUIFacade().setSelectedFromUI(!cb.isChecked());
        }
      };
      editorComposite.getEnvironment().invokeScoutLater(t, 0);
    }
  }

  protected void saveEditorFromSwt() {
    m_tableIsEditingAndContainsFocus = false;
    Runnable t = new Runnable() {
      @Override
      public void run() {
        m_tableComposite.getScoutObject().getUIFacade().completeCellEditFromUI();
      }
    };
    m_tableComposite.getEnvironment().invokeScoutLater(t, 0);
  }

  protected void cancelEditorFromSwt() {
    m_tableIsEditingAndContainsFocus = false;
    Runnable t = new Runnable() {
      @Override
      public void run() {
        m_tableComposite.getScoutObject().getUIFacade().cancelCellEditFromUI();
      }
    };
    m_tableComposite.getEnvironment().invokeScoutLater(t, 0);
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
      ITable table = m_tableComposite.getScoutObject();
      if (table != null) {
        try {
          // try first
          ITableRow row = (ITableRow) element;
          IColumn<?> column = getScoutColumn(property);
          if (row != null && column != null) {
            return table.isCellEditable(row, column);
          }
        }
        catch (Exception e) {
          //fast access: ignore
        }
      }
      return false;
    }
  }

  private class P_SwtCellEditor extends CellEditor {
    private Composite m_container;
    private Object m_value;

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
      m_container.setFocus();
      Control focusControl = m_container.getDisplay().getFocusControl();
      if (focusControl != null) {
        focusControl.addTraverseListener(new TraverseListener() {
          public void keyTraversed(TraverseEvent e) {
            if (e.detail == SWT.TRAVERSE_ESCAPE || e.detail == SWT.TRAVERSE_RETURN) {
              e.doit = false;
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
      if (e.getSource() instanceof ViewerCell) {
        ViewerCell cell = (ViewerCell) e.getSource();
        TableViewer viewer = m_tableComposite.getSwtTableViewer();
        TableColumn swtCol = viewer.getTable().getColumn(cell.getColumnIndex());
        IColumn<?> scoutCol = (IColumn<?>) swtCol.getData(SwtScoutTable.KEY_SCOUT_COLUMN);
        ITableRow scoutRow = (ITableRow) cell.getElement();
        if (scoutRow != null && scoutCol != null) {
          @SuppressWarnings("unused")
          Control control = getEditorControl(m_container, scoutRow, scoutCol);
        }
        m_container.layout();
        m_container.setVisible(true);
      }
    }

    @Override
    protected void deactivate(ColumnViewerEditorDeactivationEvent e) {
      for (Control c : m_container.getChildren()) {
        c.dispose();
      }
      super.deactivate(e);
      if (e.eventType == ColumnViewerEditorDeactivationEvent.EDITOR_CANCELED) {
        cancelEditorFromSwt();
      }
    }
  }
}
