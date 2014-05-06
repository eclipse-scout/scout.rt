/*******************************************************************************
 * Copyright (c) 2011, 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.basic.table.celleditor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorDeactivationEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableUtility;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.rap.basic.IRwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.basic.table.IRwtScoutTable;
import org.eclipse.scout.rt.ui.rap.basic.table.RwtScoutTable;
import org.eclipse.scout.rt.ui.rap.form.fields.IPopupSupport;
import org.eclipse.scout.rt.ui.rap.keystroke.RwtKeyStroke;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * This class is responsible for creating cell editors on the {@link RwtScoutTable}.
 *
 * @since 3.8.0, refactored 3.10.0-M5
 */
public class RwtScoutTableCellEditor {
  private final RwtScoutTable m_uiTableComposite;
  private final RwtScoutEditorCompositeFactory m_editorCompositeFactory;
  private final RwtScoutTableCellEditorEventHandler m_cellEditorEventHandler;

  public RwtScoutTableCellEditor(RwtScoutTable uiTableComposite) {
    m_uiTableComposite = uiTableComposite;
    m_editorCompositeFactory = new RwtScoutEditorCompositeFactory(this, m_uiTableComposite);
    m_cellEditorEventHandler = new RwtScoutTableCellEditorEventHandler(this, uiTableComposite);
  }

  //(re)install cell editors
  public void initializeUi() {
    TableViewer viewer = m_uiTableComposite.getUiTableViewer();
    String[] columnPropertyNames = new String[viewer.getTable().getColumnCount()];
    CellEditor[] oldEditors = viewer.getCellEditors();
    CellEditor[] newEditors = new CellEditor[columnPropertyNames.length];
    for (int i = 0; i < columnPropertyNames.length; i++) {
      TableColumn rwtCol = viewer.getTable().getColumn(i);
      IColumn<?> scoutCol = (IColumn<?>) rwtCol.getData(IRwtScoutTable.KEY_SCOUT_COLUMN);
      if (scoutCol != null) {
        columnPropertyNames[i] = "" + scoutCol.getColumnIndex();
        if (scoutCol.isEditable()) {
          newEditors[i] = new RwtCellEditor(viewer.getTable());
        }
      }
      else {
        columnPropertyNames[i] = "";
      }
    }
    viewer.setCellModifier(createRwtCellModifier());
    viewer.setColumnProperties(columnPropertyNames);
    viewer.setCellEditors(newEditors);
    if (oldEditors != null && oldEditors.length > 0) {
      for (CellEditor editor : oldEditors) {
        if (editor != null) {
          editor.dispose();
        }
      }
    }
  }

  protected Control getEditorControl(Composite parent, ITableRow scoutRow, IColumn<?> scoutCol) {
    //no caching
    Control swtEditorControl = null;
    IRwtScoutComposite<? extends IFormField> editorComposite = createEditorComposite(parent, scoutRow, scoutCol);
    if (editorComposite != null) {
      decorateEditorComposite(editorComposite, scoutRow, scoutCol);
      swtEditorControl = editorComposite.getUiContainer();
    }
    return swtEditorControl;
  }

  @SuppressWarnings("unchecked")
  protected IRwtScoutComposite<? extends IFormField> createEditorComposite(Composite parent, final ITableRow scoutRow, final IColumn<?> scoutCol) {
    final AtomicReference<IFormField> fieldRef = new AtomicReference<IFormField>();
    if (scoutRow != null && scoutCol != null) {
      Runnable t = new Runnable() {
        @Override
        public void run() {
          fieldRef.set(m_uiTableComposite.getScoutObject().getUIFacade().prepareCellEditFromUI(scoutRow, scoutCol));
          synchronized (fieldRef) {
            fieldRef.notifyAll();
          }
        }
      };
      synchronized (fieldRef) {
        m_uiTableComposite.getUiEnvironment().invokeScoutLater(t, 2345);
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

    IRwtScoutComposite uiScoutFormField = m_editorCompositeFactory.createEditorComposite(parent, formField, scoutRow, scoutCol);

    // If the RWT field uses a @{Shell} to edit its value, the focus on the table gets lost while the shell is open.
    // To prevent the cell editor from being closed, the focus lost listener must be suspended for the time the shell is open.
    if (uiScoutFormField instanceof IPopupSupport) {
      getRwtScoutCellEditorEventHandler().installPopupListenerOnPopupSupport((IPopupSupport) uiScoutFormField);
    }
    return uiScoutFormField;
  }

  protected void decorateEditorComposite(IRwtScoutComposite<? extends IFormField> editorComposite, final ITableRow scoutRow, final IColumn<?> scoutCol) {
  }

  protected void saveEditorFromUi() {
    Runnable t = new Runnable() {
      @Override
      public void run() {
        m_uiTableComposite.getScoutObject().getUIFacade().completeCellEditFromUI();
      }
    };
    m_uiTableComposite.getUiEnvironment().invokeScoutLater(t, 0);
  }

  protected void cancelEditorFromUi() {
    Runnable t = new Runnable() {
      @Override
      public void run() {
        m_uiTableComposite.getScoutObject().getUIFacade().cancelCellEditFromUI();
      }
    };
    m_uiTableComposite.getUiEnvironment().invokeScoutLater(t, 0);
  }

  protected void enqueueEditNextTableCell(final ITableRow row, final IColumn<?> col, final boolean forward) {
    if (row == null || col == null) {
      return;
    }
    m_uiTableComposite.getUiEnvironment().invokeScoutLater(new Runnable() {
      @Override
      public void run() {
        if (m_uiTableComposite.getUiEnvironment() == null) {
          return;
        }
        ITable table = m_uiTableComposite.getScoutObject();
        TableUtility.editNextTableCell(table, row, col, forward, new TableUtility.ITableCellEditorFilter() {
          @Override
          public boolean accept(ITableRow rowx, IColumn<?> colx) {
            return true;
          }
        });
      }
    }, 0L);
  }

  protected IColumn<?> getScoutColumn(String property) {
    if (property != null && property.matches("[0-9]+")) {
      int colIndex = Integer.parseInt(property);
      return m_uiTableComposite.getScoutObject().getColumnSet().getColumn(colIndex);
    }
    return null;
  }

  protected RwtScoutTableCellEditorEventHandler getRwtScoutCellEditorEventHandler() {
    return m_cellEditorEventHandler;
  }

  protected RwtCellModifier createRwtCellModifier() {
    return new RwtCellModifier();
  }

  protected RwtCellModifier createRwtCellModifierForFormFieldDialog(final RwtScoutFormFieldPopup formFieldDialog) {
    return new RwtCellModifier() {
      @Override
      public void modify(Object element, String property, Object value) {
        formFieldDialog.touch();
        super.modify(element, property, value);
      }
    };
  }

  class RwtCellModifier implements ICellModifier {
    private static final String DUMMY_VALUE = "Dummy";

    protected IColumn<?> getScoutColumn(String property) {
      if (property != null && property.matches("[0-9]+")) {
        int colIndex = Integer.parseInt(property);
        return m_uiTableComposite.getScoutObject().getColumnSet().getColumn(colIndex);
      }
      return null;
    }

    @Override
    public void modify(Object element, String property, Object value) {
      saveEditorFromUi();
    }

    @Override
    public Object getValue(Object element, String property) {
      return DUMMY_VALUE;
    }

    @Override
    public boolean canModify(Object element, String property) {
      final ITable table = m_uiTableComposite.getScoutObject();
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
                  b.set(table.isCellEditable(row, column));
                }
              }
              catch (Throwable ex) {
                //fast access: ignore
              }
              b.notifyAll();
            }
          }
        };
        m_uiTableComposite.getUiEnvironment().invokeScoutLater(r, 2345);
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

  class RwtCellEditor extends CellEditor {
    private static final long serialVersionUID = 1L;

    private Composite m_container;
    private Object m_value;
    private ITableRow m_editScoutRow;
    private IColumn<?> m_editScoutCol;
    private IFocusDelegate m_focusDelegate;
    private ConcurrentHashMap<IDeactivateListener, Object> m_deactivateListeners;

    protected RwtCellEditor(Table parent) {
      super(parent);
      m_focusDelegate = new P_FocusDelegate();
      m_deactivateListeners = new ConcurrentHashMap<IDeactivateListener, Object>();
    }

    @Override
    protected Control createControl(Composite/*Table*/parent) {
      m_container = new Composite(parent, SWT.NONE) {
        private static final long serialVersionUID = 1L;

        /*
         * disable inner components preferred sizes
         */
        @Override
        public Point computeSize(int wHint, int hHint, boolean changed) {
          return new Point(wHint, hHint);
        }

        @Override
        public void setBounds(Rectangle rect) {
          // ensure the check image is not visible in editor case
          if (m_editScoutCol instanceof IBooleanColumn) {
            rect.x = Math.max(0, rect.x - 16);
            rect.width = Math.max(0, rect.width + 16);
          }
          super.setBounds(rect);
        }
      };

      //The table does only dispose table items and columns so we have to manually dispose our container.
      parent.addDisposeListener(new DisposeListener() {
        private static final long serialVersionUID = 1L;

        @Override
        public void widgetDisposed(DisposeEvent event) {
          m_container.dispose();
        }

      });

      m_container.setLayout(new FillLayout());

      m_uiTableComposite.getUiEnvironment().addKeyStroke(m_container, new RwtKeyStroke(SWT.CR) {
        @Override
        public void handleUiAction(Event e) {
          e.doit = false;
          fireApplyEditorValue();
          deactivate();
        }
      }, false);

      m_container.addDisposeListener(new DisposeListener() {
        private static final long serialVersionUID = 1L;

        @Override
        public void widgetDisposed(DisposeEvent event) {
          m_uiTableComposite.getUiEnvironment().removeKeyStrokes(m_container);
        }

      });

      return m_container;
    }

    @Override
    protected void doSetFocus() {
      if (m_focusDelegate != null) {
        m_focusDelegate.doSetFocus();
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
      getRwtScoutCellEditorEventHandler().activateFocusLostListener();
      getRwtScoutCellEditorEventHandler().suspendFocusLostListener(); // is only resumed if editor gets the focus in @{link RwtCellEditor#doSetFocus()}. Otherwise, a focus lost event might be consumed and the editor closed

      m_editScoutRow = null;
      m_editScoutCol = null;
      if (e.getSource() instanceof ViewerCell) {
        ViewerCell cell = (ViewerCell) e.getSource();
        TableViewer viewer = m_uiTableComposite.getUiTableViewer();
        TableColumn rwtCol = viewer.getTable().getColumn(cell.getColumnIndex());
        IColumn<?> scoutCol = (IColumn<?>) rwtCol.getData(IRwtScoutTable.KEY_SCOUT_COLUMN);
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
      getRwtScoutCellEditorEventHandler().deregisterKeyStrokeFromFocusControl();
      getRwtScoutCellEditorEventHandler().deactivateFocusLostListener();

      // notify cell editor close listeners
      for (IDeactivateListener listener : m_deactivateListeners.keySet()) {
        if (e.eventType == ColumnViewerEditorDeactivationEvent.EDITOR_SAVED) {
          listener.saved(e);
        }
        else {
          listener.canceled(e);
        }
      }

      m_editScoutRow = null;
      m_editScoutCol = null;
      for (Control c : m_container.getChildren()) {
        c.dispose();
      }
      super.deactivate(e);
      if (e.eventType == ColumnViewerEditorDeactivationEvent.EDITOR_CANCELED) {
        cancelEditorFromUi();
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

    public IFocusDelegate getFocusDelegate() {
      return m_focusDelegate;
    }

    public void setFocusDelegate(IFocusDelegate focusDelegate) {
      m_focusDelegate = focusDelegate;
    }

    public void addDeactivateListener(IDeactivateListener listener) {
      m_deactivateListeners.put(listener, new Object());
    }

    public void removeDeactivateListener(IDeactivateListener listener) {
      m_deactivateListeners.remove(listener);
    }

    public ITableRow getScoutTableRow() {
      return m_editScoutRow;
    }

    public IColumn<?> getScoutTableColumn() {
      return m_editScoutCol;
    }

    private class P_FocusDelegate extends AbstractFocusDelegate {
      @Override
      public void doSetFocus() {
        requestFocus(m_container);
        final Control focusControl = m_container.getDisplay().getFocusControl();
        if (focusControl != null && RwtUtility.isAncestorOf(m_container, focusControl)) {
          m_uiTableComposite.getUiEnvironment().addKeyStroke(focusControl, new RwtKeyStroke(SWT.ESC) {
            @Override
            public void handleUiAction(Event e) {
              cancelCellEditing();
              m_uiTableComposite.getUiEnvironment().removeKeyStroke(focusControl, this);
              e.doit = false;
            }
          }, true);
          getRwtScoutCellEditorEventHandler().setupFocusAndTraverseListenerOnFocusControl(focusControl, RwtCellEditor.this);
        }
        getRwtScoutCellEditorEventHandler().resumeFocusLostListener();
      }
    }
  }

  /**
   * Delegate to process focus events on cell editor
   */
  interface IFocusDelegate {
    void doSetFocus();
  }

  /**
   * Listener to get notified about deactivation event
   */
  interface IDeactivateListener {
    void canceled(ColumnViewerEditorDeactivationEvent event);

    void saved(ColumnViewerEditorDeactivationEvent event);
  }

  abstract static class AbstractFocusDelegate implements IFocusDelegate {
    protected boolean requestFocus(Control control) {
      if (control == null || control.isDisposed()) {
        return false;
      }
      if (control.setFocus()) {
        return true;
      }

      if (control instanceof Composite) {
        for (Control child : ((Composite) control).getChildren()) {
          if (requestFocus(child)) {
            return true;
          }
        }
      }
      return false;
    }
  }
}
