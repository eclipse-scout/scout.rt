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
package org.eclipse.scout.rt.ui.swing.basic.table.celleditor;

import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.FocusTraversalPolicy;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.EventObject;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableUtility;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.ui.swing.SingleLayout;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.basic.ISwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.basic.table.ISwingScoutTable;
import org.eclipse.scout.rt.ui.swing.basic.table.SwingTableColumn;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.focus.SwingScoutFocusTraversalPolicy;

public class SwingScoutTableCellEditor {

  /**
   * Property to access the table cell's insets within the inline editor. The insets are registered as client property
   * in {@link JTable}.
   */
  public static final String TABLE_CELL_INSETS = SwingScoutTableCellEditor.class.getName() + "#insets";

  private ISwingScoutTable m_tableComposite;
  private FocusTraversalPolicy m_focusTraversalPolicy;
  private TableCellEditor m_cellEditor;

  private boolean m_tableIsEditingAndContainsFocus;
  private JComponent m_cachedSwingEditorComponent;
  private CellEditorListener m_cellEditorListener;

  public SwingScoutTableCellEditor(ISwingScoutTable tableComposite) {
    m_tableComposite = tableComposite;
    m_focusTraversalPolicy = new SwingScoutFocusTraversalPolicy();
    m_cellEditor = new P_SwingCellEditor();
    m_cellEditorListener = new P_CellEditorListener();
    m_cellEditor.addCellEditorListener(m_cellEditorListener);
  }

  //(re)install cell editors
  public void initialize() {
    m_tableComposite.getSwingTable().setDefaultEditor(Object.class, m_cellEditor);
  }

  protected JComponent getCachedEditorComposite(int row, int col) {
    if (m_cachedSwingEditorComponent == null) {
      ISwingScoutComposite<? extends IFormField> editorComposite = createEditorComposite(row, col);
      if (editorComposite != null) {
        decorateEditorComposite(editorComposite, row, col);
        m_cachedSwingEditorComponent = editorComposite.getSwingContainer();
      }
      else {
        m_cachedSwingEditorComponent = null;
      }
    }
    return m_cachedSwingEditorComponent;
  }

  @SuppressWarnings("unchecked")
  protected ISwingScoutComposite<? extends IFormField> createEditorComposite(int row, int col) {
    final ITableRow scoutRow = m_tableComposite.getScoutObject().getFilteredRow(row);
    final IColumn scoutColumn = m_tableComposite.getScoutObject().getColumnSet().getVisibleColumn(col);
    final AtomicReference<IFormField> fieldRef = new AtomicReference<IFormField>();
    if (scoutRow != null && scoutColumn != null) {
      Runnable t = new Runnable() {
        @Override
        public void run() {
          fieldRef.set(m_tableComposite.getScoutObject().getUIFacade().prepareCellEditFromUI(scoutRow, scoutColumn));
          synchronized (fieldRef) {
            fieldRef.notifyAll();
          }
        }
      };
      synchronized (fieldRef) {
        m_tableComposite.getSwingEnvironment().invokeScoutLater(t, 2345);
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
    // propagate insets of table cell to inline editor (to layout properly)
    Insets cellInsets = new Insets(0, 0, 0, 0);
    TableCellRenderer cellRenderer = m_tableComposite.getSwingTable().getCellRenderer(row, col);
    cellRenderer = (TableCellRenderer) m_tableComposite.getSwingTable().prepareRenderer(cellRenderer, row, col); // do not remove this call to ensure TableCellRenderer properties (e.g. insets) really belongs to the given cell (col, row). This seems to be a bug.
    if (cellRenderer instanceof DefaultTableCellRenderer) {
      cellInsets = ((DefaultTableCellRenderer) cellRenderer).getInsets();
    }

    m_tableComposite.getSwingTable().putClientProperty(SwingScoutTableCellEditor.TABLE_CELL_INSETS, cellInsets);
    try {
      // propagate vertical and horizontal alignment to @{link IBooleanField} (to layout properly)
      if (scoutColumn instanceof IBooleanColumn) {
        GridData gd = formField.getGridDataHints();
        gd.verticalAlignment = ((IBooleanColumn) scoutColumn).getVerticalAlignment();
        gd.horizontalAlignment = scoutColumn.getHorizontalAlignment();
        formField.setGridDataHints(gd);
      }

      if (formField instanceof IStringField && ((IStringField) formField).isMultilineText()) {
        // for fields to be presented as popup dialog
        return createEditorCompositesPopup(formField, row, col);
      }
      else {
        return m_tableComposite.getSwingEnvironment().createFormField(m_tableComposite.getSwingTable(), formField);
      }
    }
    finally {
      m_tableComposite.getSwingTable().putClientProperty(SwingScoutTableCellEditor.TABLE_CELL_INSETS, null);
    }
  }

  protected ISwingScoutComposite<? extends IFormField> createEditorCompositesPopup(IFormField formField, final int row, final int col) {
    // overwrite layout properties
    GridData gd = formField.getGridData();
    gd.h = 1;
    gd.w = IFormField.FULL_WIDTH;
    gd.weightY = 1;
    gd.weightX = 1;
    formField.setGridDataInternal(gd);

    int prefWidth = gd.widthInPixel;
    int minWidth = m_tableComposite.getSwingTable().getColumnModel().getColumn(col).getWidth();
    int prefHeight = gd.heightInPixel;
    int minHeight = Math.max(95, m_tableComposite.getSwingTable().getRowHeight(row));

    prefHeight = Math.max(prefHeight, minHeight);
    prefWidth = Math.max(prefWidth, minWidth);

    // listener to receive events about the popup's state
    final IFormFieldPopupEventListener popupListener = new IFormFieldPopupEventListener() {

      @Override
      public void handleEvent(FormFieldPopupEvent event) {
        if ((event.getType() & FormFieldPopupEvent.TYPE_OK) > 0) {
          // save cell editor
          m_cellEditor.stopCellEditing();
        }
        else if ((event.getType() & FormFieldPopupEvent.TYPE_CANCEL) > 0) {
          // cancel cell editor
          m_cellEditor.cancelCellEditing();
        }

        // traversal control
        if ((event.getType() & FormFieldPopupEvent.TYPE_FOCUS_BACK) > 0) {
          enqueueEditNextTableCell(row, col, false);
        }
        else if ((event.getType() & FormFieldPopupEvent.TYPE_FOCUS_NEXT) > 0) {
          enqueueEditNextTableCell(row, col, true);
        }
      }
    };

    // create placeholder field to represent the cell editor
    JPanel cellEditorPanel = new JPanel();
    cellEditorPanel.setOpaque(false);
    
    // create popup dialog to wrap the form field
    final SwingScoutFormFieldPopup formFieldDialog = new SwingScoutFormFieldPopup(cellEditorPanel);
    formFieldDialog.setMinHeight(minHeight);
    formFieldDialog.setMinWidth(minWidth);
    formFieldDialog.setPrefHeight(prefHeight);
    formFieldDialog.setPrefWidth(prefWidth);
    formFieldDialog.createField(formField, m_tableComposite.getSwingEnvironment());
    formFieldDialog.addEventListener(popupListener);

    /*
     * Wrap 'default cell editor listener' to intercept events on the cell editor.
     * This is crucial because if the user clicks on another editable cell, its cell-editor is activated prior
     * to the popup receives the WINDOW-CLOSED event (which simply is a mouse pressed event outside the dialog's boundaries) to
     * properly close the popup and write its value back to the model. In consequence, the model is not updated with the new value.
     */
    m_cellEditor.removeCellEditorListener(m_cellEditorListener);
    m_cellEditor.addCellEditorListener(new P_CellEditorListener() {

      @Override
      public void editingStopped(ChangeEvent e) {
        closePopup(FormFieldPopupEvent.TYPE_OK);
        // delegate event to default cell editor listener
        super.editingStopped(e);
      }

      @Override
      public void editingCanceled(ChangeEvent e) {
        closePopup(FormFieldPopupEvent.TYPE_CANCEL);
        // delegate event to default cell editor listener
        super.editingCanceled(e);
      }

      private void closePopup(int popupEvent) {
        try {
          // remove popup listener to not receive events on the dialog's state because the cell editor is already closing
          formFieldDialog.removeEventListener(popupListener);
          // close the popup
          formFieldDialog.closePopup(popupEvent);
        }
        finally {
          // remove wrapper to not intercept cell editor events anymore
          m_cellEditor.removeCellEditorListener(this);
          // install default cell editor listener
          m_cellEditor.addCellEditorListener(m_cellEditorListener);
        }
      }
    });

    return formFieldDialog;
  }

  protected void decorateEditorComposite(ISwingScoutComposite<? extends IFormField> editorComposite, final int row, final int col) {
    JComponent editorField = editorComposite.getSwingContainer();
    Component firstField = m_focusTraversalPolicy.getFirstComponent(editorField);
    Component lastField = m_focusTraversalPolicy.getLastComponent(editorField);
    if (firstField != null) {
      firstField.addHierarchyListener(new HierarchyListener() {
        @Override
        public void hierarchyChanged(final HierarchyEvent e) {
          if (e.getID() == HierarchyEvent.HIERARCHY_CHANGED) {
            if (((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) && e.getComponent().isShowing()) {
              SwingUtilities.invokeLater(
                  new Runnable() {
                    @Override
                    public void run() {
                      e.getComponent().requestFocus();
                    }
                  });
            }
          }
        }
      });
    }
    if (firstField instanceof JComponent) {
      JComponent jc = (JComponent) firstField;
      jc.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, new HashSet<AWTKeyStroke>());
      jc.getInputMap(JComponent.WHEN_FOCUSED).put(SwingUtility.createKeystroke("shift TAB"), "reverse-tab");
      jc.getActionMap().put("reverse-tab", new AbstractAction() {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          m_cellEditor.stopCellEditing();
          enqueueEditNextTableCell(row, col, false);
        }
      });
    }
    if (lastField instanceof JComponent) {
      JComponent jc = (JComponent) lastField;
      jc.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, new HashSet<AWTKeyStroke>());
      jc.getInputMap(JComponent.WHEN_FOCUSED).put(SwingUtility.createKeystroke("TAB"), "tab");
      jc.getActionMap().put("tab", new AbstractAction() {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          m_cellEditor.stopCellEditing();
          enqueueEditNextTableCell(row, col, true);
        }
      });
    }
  }

  protected void saveEditorFromSwing() {
    m_tableIsEditingAndContainsFocus = false;
    if (m_cachedSwingEditorComponent != null) {
      m_cachedSwingEditorComponent = null;
      Runnable t = new Runnable() {
        @Override
        public void run() {
          m_tableComposite.getScoutObject().getUIFacade().completeCellEditFromUI();
        }
      };
      m_tableComposite.getSwingEnvironment().invokeScoutLater(t, 0);
    }
  }

  protected boolean isBooleanColumnAt(Point p) {
    JTable table = m_tableComposite.getSwingTable();
    int col = table.columnAtPoint(p);
    if (col >= 0) {
      TableColumn tc = table.getColumnModel().getColumn(col);
      if (tc instanceof SwingTableColumn) {
        IColumn<?> scoutCol = ((SwingTableColumn) tc).getScoutColumn();
        return (scoutCol instanceof IBooleanColumn);
      }
    }
    return false;
  }

  protected void cancelEditorFromSwing() {
    m_tableIsEditingAndContainsFocus = false;
    if (m_cachedSwingEditorComponent != null) {
      m_cachedSwingEditorComponent = null;
      Runnable t = new Runnable() {
        @Override
        public void run() {
          m_tableComposite.getScoutObject().getUIFacade().cancelCellEditFromUI();
        }
      };
      m_tableComposite.getSwingEnvironment().invokeScoutLater(t, 0);
    }
  }

  protected void enqueueEditNextTableCell(int uiRow, int uiCol, final boolean forward) {
    if (uiRow < 0 || uiCol < 0) {
      return;
    }
    final ITableRow row = m_tableComposite.getScoutObject().getFilteredRow(uiRow);
    final IColumn col = m_tableComposite.getScoutObject().getColumnSet().getVisibleColumn(uiCol);
    if (row == null || col == null) {
      return;
    }
    m_tableComposite.getSwingEnvironment().invokeScoutLater(new Runnable() {
      @Override
      public void run() {
        if (m_tableComposite.getSwingEnvironment() == null) {
          return;
        }
        ITable table = m_tableComposite.getScoutObject();
        TableUtility.editNextTableCell(table, row, col, forward, null);
      }
    }, 0L);
  }

  protected void permanentFocusOwnerChanged(PropertyChangeEvent e) {
    Component c = (Component) e.getNewValue();
    if (c == null) {
      return;
    }
    Window w1 = SwingUtilities.getWindowAncestor(c);
    Window w2 = SwingUtilities.getWindowAncestor(m_tableComposite.getSwingContainer());
    if (w1 == null || w2 == null || w1 != w2 && SwingUtilities.isDescendingFrom(w1, w2)) {
      return;
    }
    boolean oldValue = m_tableIsEditingAndContainsFocus;
    boolean newValue = (SwingUtilities.isDescendingFrom(c, m_tableComposite.getSwingTable()) && c != m_tableComposite.getSwingTable());
    m_tableIsEditingAndContainsFocus = newValue;
    if (oldValue && !newValue) {
      if (m_cellEditor != null) {
        m_cellEditor.stopCellEditing();
      }
    }
  }

  private class P_SwingCellEditor extends AbstractCellEditor implements TableCellEditor {
    private static final long serialVersionUID = 1L;

    /**
     * An integer specifying the number of clicks needed to start editing.
     * Even if <code>clickCountToStart</code> is defined as zero, it
     * will not initiate until a click occurs.
     */
    private int m_clickCountToStart = 1;
    private JPanelEx m_container;

    public P_SwingCellEditor() {
      m_container = new JPanelEx(new SingleLayout());
      m_container.setOpaque(false);
      m_container.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(SwingUtility.createKeystroke("ESCAPE"), "cancel");
      m_container.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(SwingUtility.createKeystroke("ENTER"), "enter");
      m_container.getActionMap().put("cancel", new AbstractAction() {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          m_cellEditor.cancelCellEditing();
        }
      });
      m_container.getActionMap().put("enter", new AbstractAction() {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          m_cellEditor.stopCellEditing();
        }
      });
      //add a hysteresis listener that commits the cell editor when the table has first received focus and then lost it
      KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("permanentFocusOwner", new GlobalFocusListener(SwingScoutTableCellEditor.this));
    }

    public void setClickCountToStart(int count) {
      m_clickCountToStart = count;
    }

    public int getClickCountToStart() {
      return m_clickCountToStart;
    }

    @Override
    public Object getCellEditorValue() {
      return null;
    }

    @Override
    public boolean isCellEditable(EventObject e) {
      if (e instanceof MouseEvent) {
        //no edit on boolean column when mouse was clicked
        if (isBooleanColumnAt(((MouseEvent) e).getPoint())) {
          return false;
        }
        return ((MouseEvent) e).getClickCount() >= getClickCountToStart();
      }
      return true;
    }

    @Override
    public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, final int row, final int column) {
      m_container.removeAll();
      Component c = getCachedEditorComposite(row, column);
      if (c != null) {
        m_container.add(c);
      }
      return m_container;
    }
  }

  private static class GlobalFocusListener implements PropertyChangeListener {
    private WeakReference<SwingScoutTableCellEditor> m_editorRef;

    public GlobalFocusListener(SwingScoutTableCellEditor editor) {
      m_editorRef = new WeakReference<SwingScoutTableCellEditor>(editor);
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
      SwingScoutTableCellEditor editor = m_editorRef.get();
      if (editor == null) {
        //auto-detach
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener("permanentFocusOwner", this);
        return;
      }
      editor.permanentFocusOwnerChanged(e);
    }
  }

  private class P_CellEditorListener implements CellEditorListener {
    @Override
    public void editingStopped(ChangeEvent e) {
      saveEditorFromSwing();
    }

    @Override
    public void editingCanceled(ChangeEvent e) {
      cancelEditorFromSwing();
    }
  }

}
