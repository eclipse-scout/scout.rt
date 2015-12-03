/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class TableFieldChains {

  private TableFieldChains() {
  }

  protected abstract static class AbstractTableFieldChain<T extends ITable> extends AbstractExtensionChain<ITableFieldExtension<? extends ITable, ? extends AbstractTableField<? extends ITable>>> {

    public AbstractTableFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, ITableFieldExtension.class);
    }
  }

  public static class TableFieldReloadTableDataChain<T extends ITable> extends AbstractTableFieldChain<T> {

    public TableFieldReloadTableDataChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execReloadTableData() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableFieldExtension<? extends ITable, ? extends AbstractTableField<? extends ITable>> next) {
          next.execReloadTableData(TableFieldReloadTableDataChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class TableFieldSaveInsertedRowChain<T extends ITable> extends AbstractTableFieldChain<T> {

    public TableFieldSaveInsertedRowChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execSaveInsertedRow(final ITableRow row) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableFieldExtension<? extends ITable, ? extends AbstractTableField<? extends ITable>> next) {
          next.execSaveInsertedRow(TableFieldSaveInsertedRowChain.this, row);
        }
      };
      callChain(methodInvocation, row);
    }
  }

  public static class TableFieldSaveUpdatedRowChain<T extends ITable> extends AbstractTableFieldChain<T> {

    public TableFieldSaveUpdatedRowChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execSaveUpdatedRow(final ITableRow row) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableFieldExtension<? extends ITable, ? extends AbstractTableField<? extends ITable>> next) {
          next.execSaveUpdatedRow(TableFieldSaveUpdatedRowChain.this, row);
        }
      };
      callChain(methodInvocation, row);
    }
  }

  public static class TableFieldSaveDeletedRowChain<T extends ITable> extends AbstractTableFieldChain<T> {

    public TableFieldSaveDeletedRowChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execSaveDeletedRow(final ITableRow row) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableFieldExtension<? extends ITable, ? extends AbstractTableField<? extends ITable>> next) {
          next.execSaveDeletedRow(TableFieldSaveDeletedRowChain.this, row);
        }
      };
      callChain(methodInvocation, row);
    }
  }

  public static class TableFieldSaveChain<T extends ITable> extends AbstractTableFieldChain<T> {

    public TableFieldSaveChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execSave(final List<? extends ITableRow> insertedRows, final List<? extends ITableRow> updatedRows, final List<? extends ITableRow> deletedRows) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableFieldExtension<? extends ITable, ? extends AbstractTableField<? extends ITable>> next) {
          next.execSave(TableFieldSaveChain.this, insertedRows, updatedRows, deletedRows);
        }
      };
      callChain(methodInvocation, insertedRows, updatedRows, deletedRows);
    }
  }
}
