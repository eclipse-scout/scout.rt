package org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
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

    public void execReloadTableData() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableFieldExtension<? extends ITable, ? extends AbstractTableField<? extends ITable>> next) throws ProcessingException {
          next.execReloadTableData(TableFieldReloadTableDataChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TableFieldUpdateTableStatusChain<T extends ITable> extends AbstractTableFieldChain<T> {

    public TableFieldUpdateTableStatusChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execUpdateTableStatus() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableFieldExtension<? extends ITable, ? extends AbstractTableField<? extends ITable>> next) {
          next.execUpdateTableStatus(TableFieldUpdateTableStatusChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class TableFieldSaveInsertedRowChain<T extends ITable> extends AbstractTableFieldChain<T> {

    public TableFieldSaveInsertedRowChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execSaveInsertedRow(final ITableRow row) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableFieldExtension<? extends ITable, ? extends AbstractTableField<? extends ITable>> next) throws ProcessingException {
          next.execSaveInsertedRow(TableFieldSaveInsertedRowChain.this, row);
        }
      };
      callChain(methodInvocation, row);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TableFieldSaveUpdatedRowChain<T extends ITable> extends AbstractTableFieldChain<T> {

    public TableFieldSaveUpdatedRowChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execSaveUpdatedRow(final ITableRow row) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableFieldExtension<? extends ITable, ? extends AbstractTableField<? extends ITable>> next) throws ProcessingException {
          next.execSaveUpdatedRow(TableFieldSaveUpdatedRowChain.this, row);
        }
      };
      callChain(methodInvocation, row);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TableFieldSaveDeletedRowChain<T extends ITable> extends AbstractTableFieldChain<T> {

    public TableFieldSaveDeletedRowChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execSaveDeletedRow(final ITableRow row) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableFieldExtension<? extends ITable, ? extends AbstractTableField<? extends ITable>> next) throws ProcessingException {
          next.execSaveDeletedRow(TableFieldSaveDeletedRowChain.this, row);
        }
      };
      callChain(methodInvocation, row);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

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
