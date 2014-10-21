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

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorDeactivationEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.scout.commons.BooleanUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
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
import org.eclipse.scout.rt.ui.swt.keystroke.SwtKeyStroke;
import org.eclipse.scout.rt.ui.swt.util.SwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
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
 * Editable support for {@link ITable} in SWT-UI.
 */
public class SwtScoutTableCellEditor {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutTableCellEditor.class);

  private static final String DUMMY_VALUE = "Dummy";

  private final ISwtScoutTable m_tableComposite;
  private final Listener m_rowHeightListener;

  private P_FocusLostListener m_focusLostListener;

  public SwtScoutTableCellEditor(final ISwtScoutTable tableComposite) {
    m_focusLostListener = new P_FocusLostListener();
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
          newEditors[i] = new P_SwtCellEditor(viewer.getTable(), scoutCol);
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

  @SuppressWarnings("unchecked")
  protected Control createEditorControl(Composite parent, final ITableRow scoutRow, final IColumn<?> scoutCol) {
    IFormField formField = createFormField(scoutRow, scoutCol);
    if (formField == null) {
      return null;
    }

    ISwtScoutComposite swtScoutFormField;
    if (formField instanceof IStringField && ((IStringField) formField).isMultilineText()) {
      // open a separate Shell to edit the content.
      swtScoutFormField = createEditorCompositePopup(parent, formField, scoutRow, scoutCol);
    }
    else {
      swtScoutFormField = m_tableComposite.getEnvironment().createFormField(parent, formField);
    }

    if (swtScoutFormField != null) {
      decorateEditorComposite(swtScoutFormField, scoutRow, scoutCol);
      return swtScoutFormField.getSwtContainer();
    }
    else {
      return null;
    }
  }

  protected ISwtScoutComposite<? extends IFormField> createEditorCompositePopup(final Composite parent, IFormField formField, final ITableRow scoutRow, final IColumn<?> scoutCol) {
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

    // Create placeholder field to represent the cell editor
    final Composite cellEditorComposite = new Composite(parent, SWT.NONE);

    // Create popup dialog to wrap the form field
    final SwtScoutFormFieldPopup formFieldDialog = new SwtScoutFormFieldPopup(cellEditorComposite);
    formFieldDialog.setPrefHeight(prefHeight);
    formFieldDialog.setPrefWidth(prefWidth);
    formFieldDialog.setMinHeight(minHeight);
    formFieldDialog.setMinWidth(minWidth);

    // == ICellModifier ==
    // Replace default cell-modifier strategy to touch form-field to be written back to the model.
    final ICellModifier defaultCellModifier = m_tableComposite.getSwtTableViewer().getCellModifier();
    m_tableComposite.getSwtTableViewer().setCellModifier(new P_SwtCellModifier() {

      @Override
      public void modify(Object element, String property, Object value) {
        formFieldDialog.touch();
        super.modify(element, property, value);
      }
    });

    // == IFocusDelegate ==
    // Replace default focus handling strategy.
    final IFocusDelegate defaultFocusDelegate = cellEditor.getFocusDelegate();
    cellEditor.setFocusDelegate(new IFocusDelegate() {

      @Override
      public void doSetFocus() {
        // NOOP: Focus is set the time the Shell is opened.
      }
    });

    // == IFormFieldPopupListener ==
    // To receive events about the popup's state. The popup is not closed yet but the cell-editor closed.
    final IFormFieldPopupListener formFieldPopupListener = new IFormFieldPopupListener() {

      @Override
      public void handleEvent(int event) {
        if ((event & IFormFieldPopupListener.TYPE_OK) > 0) {
          cellEditor.stopCellEditing(); // save cell editor
        }
        else if ((event & IFormFieldPopupListener.TYPE_CANCEL) > 0) {
          cellEditor.cancelCellEditing(); // cancel cell editor
        }

        // traversal control
        if ((event & IFormFieldPopupListener.TYPE_FOCUS_BACK) > 0) {
          enqueueEditNextTableCell(scoutRow, scoutCol, false);
        }
        else if ((event & IFormFieldPopupListener.TYPE_FOCUS_NEXT) > 0) {
          enqueueEditNextTableCell(scoutRow, scoutCol, true);
        }
      }
    };
    formFieldDialog.addListener(formFieldPopupListener);

    // == DisposeListener ==
    // To close the Shell if the cell-editor is disposed.
    cellEditorComposite.addDisposeListener(new DisposeListener() {

      @Override
      public void widgetDisposed(DisposeEvent e) {
        formFieldDialog.removeListener(formFieldPopupListener); // ignore resulting popup events.

        // Restore default focus delegate and cell modifier.
        cellEditor.setFocusDelegate(defaultFocusDelegate);
        m_tableComposite.getSwtTableViewer().setCellModifier(defaultCellModifier);

        // Close the popup Shell.
        // The asyncExec is a workaround so that other cell-editors can be activated immediately.
        // Note: If being dirty, 'Viewer#refresh()' in TableEx prevents the cell from being activated immediately.
        e.display.asyncExec(new Runnable() {

          @Override
          public void run() {
            formFieldDialog.closePopup();
          }
        });
      }
    });

    // Open the popup for the form field.
    formFieldDialog.createField(parent, formField, m_tableComposite.getEnvironment());

    return formFieldDialog;
  }

  protected IFormField createFormField(final ITableRow scoutRow, final IColumn<?> scoutCol) {
    if (scoutRow == null || scoutCol == null) {
      return null;
    }

    final Holder<IFormField> result = new Holder<IFormField>();
    Runnable t = new Runnable() {
      @Override
      public void run() {
        result.setValue(m_tableComposite.getScoutObject().getUIFacade().prepareCellEditFromUI(scoutRow, scoutCol));
      }
    };
    try {
      m_tableComposite.getEnvironment().invokeScoutLater(t, 2345).join(2345);
    }
    catch (InterruptedException e) {
      LOG.warn("Interrupted while waiting for the Form-Field to be created.", e);
    }
    return result.getValue();
  }

  /**
   * Callback to be overwritten to customize the {@link IFormField}.
   */
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
            return true;
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

      final BooleanHolder result = new BooleanHolder();
      Runnable r = new Runnable() {
        @Override
        public void run() {
          if (table != null && row != null && column != null) {
            result.setValue(table.isCellEditable(row, column));
          }
        }
      };
      try {
        m_tableComposite.getEnvironment().invokeScoutLater(r, 2345).join(2345);
      }
      catch (InterruptedException e) {
        LOG.warn("Interrupted while waiting for the model to determine the cell's editability.", e);
      }
      return BooleanUtility.nvl(result.getValue(), false);
    }
  }

  /**
   * Statefull per-column cell-editor which is used for all cells of a column.
   */
  private class P_SwtCellEditor extends CellEditor {
    private Composite m_container;
    private Object m_value;
    private ITableRow m_editScoutRow;
    private IFocusDelegate m_focusDelegate;
    private IColumn<?> m_scoutCol;
    private ViewerCell m_cell;
    private Image m_image;

    /**
     * @param parent
     *          the table.
     * @param scoutCol
     *          the scout column this cell editor is used for.
     */
    protected P_SwtCellEditor(Composite parent, IColumn<?> scoutCol) {
      super(parent);
      m_scoutCol = scoutCol;
      m_focusDelegate = new P_FocusDelegate();
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
      m_tableComposite.getEnvironment().addKeyStroke(m_container, new SwtKeyStroke(SWT.KEYPAD_CR) {
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
      m_focusDelegate.doSetFocus();
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
      // Install a focus-lost listener on the table widget to close an active cell-editor when the table looses the focus.
      m_focusLostListener.install();

      if (!(e.getSource() instanceof ViewerCell)) {
        return;
      }

      m_cell = (ViewerCell) e.getSource();
      m_editScoutRow = (ITableRow) m_cell.getElement();

      if (m_scoutCol instanceof IBooleanColumn) {
        if (e.sourceEvent instanceof MouseEvent) {
          return; // no edit-mode when a boolean cell was clicked by mouse.
        }
        else {
          // hide the checkbox image when editing a boolean value in traversal-mode.
          m_image = m_cell.getImage();
          m_cell.setImage(null);
        }
      }

      // create the cell editor widget.
      if (m_editScoutRow != null) {
        createEditorControl(m_container, m_editScoutRow, m_scoutCol);
      }

      m_container.layout(true, true);
      m_container.setVisible(true);
    }

    @Override
    protected void deactivate(ColumnViewerEditorDeactivationEvent e) {
      // restore the cell's image if being unset in CellEditor#activate.
      if (m_cell != null && m_image != null) {
        m_cell.setImage(m_image);
      }

      m_cell = null;
      m_image = null;
      m_editScoutRow = null;

      // Dispose the cell-editor; in turn, any Shell opened by the editor is closed as well.
      for (Control c : m_container.getChildren()) {
        c.dispose();
      }
      if (e.eventType == ColumnViewerEditorDeactivationEvent.EDITOR_CANCELED) {
        cancelEditorFromSwt();
      }

      super.deactivate(e);

      m_focusLostListener.uninstall();
    }

    @Override
    protected boolean dependsOnExternalFocusListener() {
      return false;
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

    private class P_FocusDelegate implements IFocusDelegate {

      @Override
      public void doSetFocus() {
        // traverse the focus to the cell editor's control so that the user can start editing immediately without having to click into the widget first.
        m_container.traverse(SWT.TRAVERSE_TAB_NEXT);

        Control focusControl = m_container.getDisplay().getFocusControl();
        if (focusControl != null && SwtUtility.isAncestorOf(m_container, focusControl)) {
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
                  ITableRow currentScoutRow = m_editScoutRow; // memorize the current row because being set to null when the cell editor is deactivated.
                  fireApplyEditorValue();
                  deactivate();
                  enqueueEditNextTableCell(currentScoutRow, m_scoutCol, true); // traverse the focus to the next editable cell.
                  break;
                }
                case SWT.TRAVERSE_TAB_PREVIOUS: {
                  e.doit = false;
                  ITableRow currentScoutRow = m_editScoutRow; // memorize the current row because being set to null when the cell editor is deactivated.
                  fireApplyEditorValue();
                  deactivate();
                  enqueueEditNextTableCell(currentScoutRow, m_scoutCol, false); // traverse the focus to the next editable cell.
                  break;
                }
              }
            }
          });
        }
      }
    }
  }

  /**
   * Hysteresis listener that commits the cell editor when the table has first received focus and then lost it. That is
   * because cell editors in SWT are not closed automatically if the table looses the focus.
   */
  private class P_FocusLostListener implements Listener {

    /**
     * Installs listening for focus-lost events on the table widget.
     */
    public void install() {
      m_tableComposite.getEnvironment().getDisplay().addFilter(SWT.FocusIn, this);
    }

    /**
     * Uninstalls listening for focus-lost events on the table widget.
     */
    public void uninstall() {
      m_tableComposite.getEnvironment().getDisplay().removeFilter(SWT.FocusIn, this);
    }

    @Override
    public void handleEvent(Event event) {
      Widget w = event.widget;
      if (w == null || !(w instanceof Control) || w.isDisposed()) {
        return;
      }

      // Sanity check whether a cell-editor is active.
      TableViewer viewer = m_tableComposite.getSwtTableViewer();
      if (!viewer.isCellEditorActive()) {
        return;
      }

      Control focusOwner = (Control) w;
      Table table = m_tableComposite.getSwtTableViewer().getTable();

      // Check if the table is the focus owner.
      if (SwtUtility.isAncestorOf(table, focusOwner)) {
        return;
      }

      // Check if a Shell opened by the cell-editor is the focus owner.
      if (focusOwner.getShell() != table.getShell()) {
        Composite parentFocusOwner = focusOwner.getShell().getParent();
        while (parentFocusOwner != null) {
          if (parentFocusOwner.getShell() == table.getShell()) {
            return; // focus owner is a derrived Shell.
          }
          else {
            parentFocusOwner = parentFocusOwner.getShell().getParent();
          }
        }
      }

      // Close the cell-editor because a control other than the table is focus owner.
      for (CellEditor editor : viewer.getCellEditors()) {
        if (editor != null && editor.isActivated() && editor instanceof P_SwtCellEditor) {
          ((P_SwtCellEditor) editor).stopCellEditing();
          break;
        }
      }
    }
  }

  /**
   * Delegate to process focus events on cell editor.
   */
  private interface IFocusDelegate {
    void doSetFocus();
  }

  /**
   * Listener to get notified about deactivation events.
   */
  private interface IDeactivateListener {
    void canceled(ColumnViewerEditorDeactivationEvent event);

    void saved(ColumnViewerEditorDeactivationEvent event);
  }
}
