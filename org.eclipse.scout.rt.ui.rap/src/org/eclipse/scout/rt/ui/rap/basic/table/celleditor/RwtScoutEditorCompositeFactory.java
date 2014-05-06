/*******************************************************************************
 * Copyright (c) 2010, 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.rap.basic.table.celleditor;

import org.eclipse.jface.viewers.ColumnViewerEditorDeactivationEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.ui.rap.basic.IRwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.basic.table.IRwtScoutTable;
import org.eclipse.scout.rt.ui.rap.basic.table.RwtScoutTable;
import org.eclipse.scout.rt.ui.rap.basic.table.celleditor.RwtScoutTableCellEditor.IDeactivateListener;
import org.eclipse.scout.rt.ui.rap.basic.table.celleditor.RwtScoutTableCellEditor.RwtCellEditor;
import org.eclipse.scout.rt.ui.rap.form.IRwtScoutForm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * This class is responsible for creating an editor composite either with a normal editing field or
 * a special field as a pop-up for editing texts with multiple lines.
 * 
 * @since 3.10.0-M5
 */
public class RwtScoutEditorCompositeFactory {

  private final RwtScoutTableCellEditor m_tableCellEditor;
  private final RwtScoutTable m_uiTableComposite;

  public RwtScoutEditorCompositeFactory(RwtScoutTableCellEditor tableCellEditor, RwtScoutTable uiTableComposite) {
    m_tableCellEditor = tableCellEditor;
    m_uiTableComposite = uiTableComposite;
  }

  @SuppressWarnings("unchecked")
  public IRwtScoutComposite<? extends IFormField> createEditorComposite(Composite parent, final IFormField formField, final ITableRow scoutRow, final IColumn<?> scoutCol) {
    if (formField instanceof IStringField && ((IStringField) formField).isMultilineText()) {
      // for fields to be presented as popup dialog
      return createEditorCompositeAsPopup(parent, formField, scoutRow, scoutCol);
    }
    return m_uiTableComposite.getUiEnvironment().createFormField(parent, formField);
  }

  protected IRwtScoutComposite<? extends IFormField> createEditorCompositeAsPopup(Composite parent, final IFormField formField, final ITableRow scoutRow, final IColumn<?> scoutCol) {
    // overwrite layout properties
    GridData gd = formField.getGridData();
    gd.h = 1;
    gd.w = IFormField.FULL_WIDTH;
    gd.weightY = 1;
    gd.weightX = 1;
    formField.setGridDataInternal(gd);

    TableColumn swtCol = getRwtColumn(scoutCol);
    final RwtCellEditor cellEditor = (RwtCellEditor) m_uiTableComposite.getUiTableViewer().getCellEditors()[getRwtColumnIndex(swtCol)];

    int prefWidth = gd.widthInPixel;
    int minWidth = swtCol.getWidth();
    int prefHeight = gd.heightInPixel;
    int minHeight = Math.max(105, m_uiTableComposite.getUiTableViewer().getTable().getItemHeight());

    prefHeight = Math.max(prefHeight, minHeight);
    prefWidth = Math.max(prefWidth, minWidth);

    // create placeholder field to represent the cell editor
    Composite cellEditorComposite = new Composite(parent, SWT.NONE);

    // create popup dialog to wrap the form field
    final RwtScoutFormFieldPopup formFieldDialog = new RwtScoutFormFieldPopup(cellEditorComposite);
    formFieldDialog.setPrefHeight(prefHeight);
    formFieldDialog.setPrefWidth(prefWidth);
    formFieldDialog.setMinHeight(minHeight);
    formFieldDialog.setMinWidth(minWidth);
    formFieldDialog.createUiField(parent, formField, m_uiTableComposite.getUiEnvironment());

    // register custom cell modifier to touch the field in order to write its value back to the model
    final ICellModifier defaultCellModifier = m_uiTableComposite.getUiTableViewer().getCellModifier();
    m_uiTableComposite.getUiTableViewer().setCellModifier(m_tableCellEditor.createRwtCellModifierForFormFieldDialog(formFieldDialog));
    // register custom focus delegate to request the field's focus
    final RwtScoutTableCellEditor.IFocusDelegate defaultFocusDelegate = cellEditor.getFocusDelegate();
    cellEditor.setFocusDelegate(new RwtScoutTableCellEditor.AbstractFocusDelegate() {

      @Override
      public void doSetFocus() {
        final IRwtScoutForm rwtScoutForm = formFieldDialog.getInnerRwtScoutForm();
        if (rwtScoutForm != null) {
          requestFocus(rwtScoutForm.getUiContainer());
        }
      }
    });

    // listener to receive events about the popup's state
    final IFormFieldPopupEventListener popupListener = new IFormFieldPopupEventListener() {

      @Override
      public void handleEvent(FormFieldPopupEvent event) {
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
          m_tableCellEditor.enqueueEditNextTableCell(scoutRow, scoutCol, false);
        }
        else if ((event.getType() & FormFieldPopupEvent.TYPE_FOCUS_NEXT) > 0) {
          m_tableCellEditor.enqueueEditNextTableCell(scoutRow, scoutCol, true);
        }
      }
    };
    formFieldDialog.addEventListener(popupListener);

    // register listener to intercept the cell editor's events in order to properly close the popup.
    // This is crucial if the editor is deactivated programmatically or if another cell is activated.
    // In contrast to SWT, in RWT the next cell is activated prior to receiving the shell closed event.
    cellEditor.addDeactivateListener(new IDeactivateListener() {

      @Override
      public void canceled(ColumnViewerEditorDeactivationEvent event) {
        restoreDefault();
        closePopup(FormFieldPopupEvent.TYPE_OK);
      }

      @Override
      public void saved(ColumnViewerEditorDeactivationEvent event) {
        restoreDefault();
        closePopup(FormFieldPopupEvent.TYPE_CANCEL);
      }

      private void restoreDefault() {
        // restore default focus delegate
        cellEditor.setFocusDelegate(defaultFocusDelegate);
        // restore default cell modifier
        m_uiTableComposite.getUiTableViewer().setCellModifier(defaultCellModifier);
        // remove this listener on the cell editor
        cellEditor.removeDeactivateListener(this);
      }

      private void closePopup(int popupEvent) {
        if (formFieldDialog.isClosed()) {
          return;
        }
        // remove popup listener to not receive events on the dialog's state because the cell editor is already closing
        formFieldDialog.removeEventListener(popupListener);
        // close the popup
        formFieldDialog.closePopup(popupEvent);
      }
    });

    return formFieldDialog;
  }

  private TableColumn getRwtColumn(IColumn<?> scoutCol) {
    for (TableColumn swtCol : m_uiTableComposite.getUiTableViewer().getTable().getColumns()) {
      IColumn<?> candidate = (IColumn<?>) swtCol.getData(IRwtScoutTable.KEY_SCOUT_COLUMN);
      if (candidate != null && CompareUtility.equals(candidate.getColumnId(), scoutCol.getColumnId())) {
        return swtCol;
      }
    }
    return null;
  }

  private int getRwtColumnIndex(TableColumn swtCol) {
    Table table = m_uiTableComposite.getUiTableViewer().getTable();
    for (int i = 0; i < table.getColumnCount(); i++) {
      if (table.getColumn(i) == swtCol) {
        return i;
      }
    }
    return -1;
  }
}
