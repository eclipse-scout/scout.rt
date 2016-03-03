/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
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
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.basic.IInputVerifiable;
import org.eclipse.scout.rt.ui.swt.basic.IInputVerifyListener;
import org.eclipse.scout.rt.ui.swt.basic.ISwtScoutComposite;
import org.eclipse.scout.rt.ui.swt.basic.table.ISwtScoutTable;
import org.eclipse.scout.rt.ui.swt.basic.table.SwtScoutTable;
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
 * <p>
 * {@link CellEditor} for {@link SwtScoutTable}.
 * </p>
 * Each editable cell has its own {@link CellEditor} instance.
 */
public class TableCellEditor extends CellEditor {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(TableCellEditor.class);

  private ISwtEnvironment m_environment;
  private P_FocusLostListener m_focusLostListener;

  private Composite m_container;
  private Image m_image;

  private IColumn<?> m_scoutColumn;
  private ITableRow m_scoutRow;
  private ITable m_scoutTable;
  private TableColumn m_swtColumn;
  private Table m_swtTable;

  private TableViewer m_tableViewer;

  private boolean m_requestFocus;

  public TableCellEditor(TableViewer tableViewer, TableColumn swtColumn, ITableRow scoutRow, ISwtEnvironment environment) {
    super(tableViewer.getTable());
    m_scoutRow = scoutRow;
    m_scoutColumn = (IColumn<?>) swtColumn.getData(ISwtScoutTable.KEY_SCOUT_COLUMN);
    m_scoutTable = m_scoutColumn.getTable();
    m_swtColumn = swtColumn;
    m_tableViewer = tableViewer;
    m_swtTable = tableViewer.getTable();

    m_environment = environment;
    m_focusLostListener = new P_FocusLostListener();
  }

  @Override
  protected Control createControl(Composite parent) {
    m_container = new Composite(parent, SWT.NONE) {
      @Override
      // disable inner components preferred sizes.
      public Point computeSize(int wHint, int hHint, boolean changed) {
        return new Point(wHint, hHint);
      }
    };
    m_container.setLayout(new FillLayout());

    return m_container;
  }

  @Override
  protected Object doGetValue() {
    // NOOP: The value is written back to the model by the widget's verify event.
    return null;
  }

  @Override
  protected void doSetValue(Object value) {
    // NOOP: The value is set into the cell-editor when it is created.
  }

  @Override
  public void activate(ColumnViewerEditorActivationEvent e) {
    m_requestFocus = true;

    // Install a focus-lost listener on the table widget to close an active cell-editor when the table looses the focus.
    m_focusLostListener.install();

    // Install keystrokes to exit editing mode.
    m_environment.addKeyStroke(m_container, new SwtKeyStroke(SWT.ESC) {
      @Override
      public void handleSwtAction(Event event) {
        event.doit = false;
        fireCancelEditor();
      }
    });
    m_environment.addKeyStroke(m_container, new SwtKeyStroke(SWT.CR) {
      @Override
      public void handleSwtAction(Event event) {
        event.doit = false;
        fireApplyEditorValue();
      }
    });
    m_environment.addKeyStroke(m_container, new SwtKeyStroke(SWT.KEYPAD_CR) {
      @Override
      public void handleSwtAction(Event event) {
        event.doit = false;
        fireApplyEditorValue();
      }
    });

    // Specific cell-editor for boolean values.
    if (m_scoutColumn instanceof IBooleanColumn) {
      if (e.sourceEvent instanceof MouseEvent) {
        // no edit-mode when a boolean cell was clicked by mouse because being inverted and the editing mode closed in AbstractTable#interceptRowClickSingleObserver.
        m_requestFocus = false;
        return;
      }
    }
    // always hide the image when editing
    ViewerCell cell = (ViewerCell) e.getSource();
    m_image = cell.getImage();
    cell.setImage(null);

    // create the Scout model field.
    IFormField formField = createFormField();
    if (formField == null) {
      LOG.warn("Failed to create FormField for cell-editor; editing mode canceled.");
      m_requestFocus = false;
      fireCancelEditor();
      return;
    }

    // create the UI field.
    ISwtScoutComposite swtScoutFormField;
    if (formField instanceof IStringField && ((IStringField) formField).isMultilineText()) {
      // open a separate Shell to edit the content.
      swtScoutFormField = createPopupEditorControl(m_container, formField);
    }
    else {
      swtScoutFormField = m_environment.createFormField(m_container, formField);
    }
    // add a input verify listener to ensure the editor gets closed
    if (swtScoutFormField instanceof IInputVerifiable) {
      ((IInputVerifiable) swtScoutFormField).addInputVerifyListener(new IInputVerifyListener() {

        @Override
        public void inputVerified() {
          fireApplyEditorValue();
        }
      });
    }
    // hook to customize the form field.
    decorateEditorComposite(swtScoutFormField, m_scoutRow, m_scoutColumn);

    m_container.layout(true, true);
    m_container.setVisible(true);
  }

  @Override
  protected void doSetFocus() {
    if (!m_requestFocus) {
      return;
    }

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
              fireApplyEditorValue();
              enqueueEditNextTableCell(true); // traverse the focus to the next editable cell.
              break;
            }
            case SWT.TRAVERSE_TAB_PREVIOUS: {
              e.doit = false;
              fireApplyEditorValue();
              enqueueEditNextTableCell(false); // traverse the focus to the next editable cell.
              break;
            }
          }
        }
      });
    }
  }

  @Override
  protected void deactivate(ColumnViewerEditorDeactivationEvent e) {
    // restore the cell's image if being unset in CellEditor#activate.
    ViewerCell cell = (ViewerCell) e.getSource();
    if (m_image != null) {
      cell.setImage(m_image);
    }

    m_image = null;

    // Dispose the cell-editor; in turn, any Shell opened by the editor is closed as well.
    for (Control c : m_container.getChildren()) {
      c.dispose();
    }

    super.deactivate(e);

    m_focusLostListener.uninstall();
  }

  @Override
  protected boolean dependsOnExternalFocusListener() {
    return false;
  }

  protected void enqueueEditNextTableCell(final boolean forward) {
    m_environment.invokeScoutLater(new Runnable() {
      @Override
      public void run() {
        ITable table = m_scoutColumn.getTable();
        TableUtility.editNextTableCell(table, m_scoutRow, m_scoutColumn, forward, new TableUtility.ITableCellEditorFilter() {
          @Override
          public boolean accept(ITableRow rowx, IColumn<?> colx) {
            return true;
          }
        });
      }
    }, 0L);
  }

  protected ISwtScoutComposite<? extends IFormField> createPopupEditorControl(final Composite parent, IFormField formField) {
    // overwrite layout properties
    GridData gd = formField.getGridData();
    gd.h = 1;
    gd.w = IFormField.FULL_WIDTH;
    gd.weightY = 1;
    gd.weightX = 1;
    formField.setGridDataInternal(gd);

    int prefWidth = gd.widthInPixel;
    int minWidth = m_swtColumn.getWidth();
    int prefHeight = gd.heightInPixel;
    int minHeight = Math.max(105, m_swtTable.getItemHeight());

    prefHeight = Math.max(prefHeight, minHeight);
    prefWidth = Math.max(prefWidth, minWidth);

    // Create placeholder field to represent the cell editor
    final Composite cellEditorComposite = new Composite(parent, SWT.NONE);

    // Create popup dialog to wrap the form field
    final SwtScoutFormFieldPopup popup = new SwtScoutFormFieldPopup(cellEditorComposite);
    popup.setPrefHeight(prefHeight);
    popup.setPrefWidth(prefWidth);
    popup.setMinHeight(minHeight);
    popup.setMinWidth(minWidth);

    // Focus is set the time the Shell is opened.
    m_requestFocus = false;

    // == IFormFieldPopupListener ==
    // To receive events about the popup's state. The popup is not closed yet but the cell-editor closed.
    final IFormFieldPopupListener formFieldPopupListener = new IFormFieldPopupListener() {

      @Override
      public void handleEvent(int event) {
        if ((event & IFormFieldPopupListener.TYPE_OK) > 0) {
          SwtUtility.runSwtInputVerifier(popup.getSwtField()); // write the value back into the model.
          fireApplyEditorValue();
        }
        else if ((event & IFormFieldPopupListener.TYPE_CANCEL) > 0) {
          fireCancelEditor();
        }

        // traversal control
        if ((event & IFormFieldPopupListener.TYPE_FOCUS_BACK) > 0) {
          enqueueEditNextTableCell(false);
        }
        else if ((event & IFormFieldPopupListener.TYPE_FOCUS_NEXT) > 0) {
          enqueueEditNextTableCell(true);
        }
      }
    };
    popup.addListener(formFieldPopupListener);

    // == DisposeListener ==
    // To close the Shell if the cell-editor is disposed.
    cellEditorComposite.addDisposeListener(new DisposeListener() {

      @Override
      public void widgetDisposed(DisposeEvent e) {
        popup.removeListener(formFieldPopupListener); // ignore resulting popup events.

        // Close the popup Shell.
        // The asyncExec is a workaround so that other cell-editors can be activated immediately.
        // Note: If being dirty, 'Viewer#refresh()' in TableEx prevents the cell from being activated immediately.
        e.display.asyncExec(new Runnable() {

          @Override
          public void run() {
            popup.closePopup();
          }
        });
      }
    });

    // Open the popup for the form field.
    popup.createField(parent, formField, m_environment);

    return popup;
  }

  protected IFormField createFormField() {
    final Holder<IFormField> result = new Holder<IFormField>();
    Runnable t = new Runnable() {
      @Override
      public void run() {
        result.setValue(m_scoutTable.getUIFacade().prepareCellEditFromUI(m_scoutRow, m_scoutColumn));
      }
    };
    try {
      m_environment.invokeScoutLater(t, 2345).join(2345);
    }
    catch (InterruptedException e) {
      LOG.warn("Interrupted while waiting for the Form-Field to be created.", e);
    }
    return result.getValue();
  }

  /**
   * Callback to be overwritten to customize the {@link IFormField}.
   */
  protected void decorateEditorComposite(ISwtScoutComposite editorComposite, final ITableRow scoutRow, final IColumn<?> scoutCol) {
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
      m_environment.getDisplay().addFilter(SWT.FocusIn, this);
    }

    /**
     * Uninstalls listening for focus-lost events on the table widget.
     */
    public void uninstall() {
      m_environment.getDisplay().removeFilter(SWT.FocusIn, this);
    }

    @Override
    public void handleEvent(Event event) {
      Widget w = event.widget;
      if (w == null || !(w instanceof Control) || w.isDisposed()) {
        return;
      }

      // Sanity check whether a cell-editor is active.
      TableViewer viewer = m_tableViewer;
      if (!viewer.isCellEditorActive()) {
        return;
      }

      Control focusOwner = (Control) w;
      Table table = m_tableViewer.getTable();

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
      fireApplyEditorValue();
    }
  }
}
