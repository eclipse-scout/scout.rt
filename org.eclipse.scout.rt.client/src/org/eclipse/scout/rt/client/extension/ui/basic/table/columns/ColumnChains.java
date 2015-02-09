package org.eclipse.scout.rt.client.extension.ui.basic.table.columns;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.HeaderCell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class ColumnChains {

  private ColumnChains() {
  }

  protected abstract static class AbstractColumnChain<VALUE> extends AbstractExtensionChain<IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> {

    public AbstractColumnChain(List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions) {
      super(extensions, IColumnExtension.class);
    }
  }

  public static class ColumnCompleteEditChain<VALUE> extends AbstractColumnChain<VALUE> {

    public ColumnCompleteEditChain(List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions) {
      super(extensions);
    }

    public void execCompleteEdit(final ITableRow row, final IFormField editingField) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>> next) throws ProcessingException {
          next.execCompleteEdit(ColumnCompleteEditChain.this, row, editingField);
        }
      };
      callChain(methodInvocation, row, editingField);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class ColumnInitColumnChain<VALUE> extends AbstractColumnChain<VALUE> {

    public ColumnInitColumnChain(List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions) {
      super(extensions);
    }

    public void execInitColumn() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>> next) throws ProcessingException {
          next.execInitColumn(ColumnInitColumnChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class ColumnParseValueChain<VALUE> extends AbstractColumnChain<VALUE> {

    public ColumnParseValueChain(List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions) {
      super(extensions);
    }

    public VALUE execParseValue(final ITableRow row, final Object rawValue) throws ProcessingException {
      MethodInvocation<VALUE> methodInvocation = new MethodInvocation<VALUE>() {
        @Override
        protected void callMethod(IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>> next) throws ProcessingException {
          setReturnValue(next.execParseValue(ColumnParseValueChain.this, row, rawValue));
        }
      };
      callChain(methodInvocation, row, rawValue);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
      return methodInvocation.getReturnValue();
    }
  }

  public static class ColumnIsEditableChain<VALUE> extends AbstractColumnChain<VALUE> {

    public ColumnIsEditableChain(List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions) {
      super(extensions);
    }

    public boolean execIsEditable(final ITableRow row) throws ProcessingException {
      MethodInvocation<Boolean> methodInvocation = new MethodInvocation<Boolean>() {
        @Override
        protected void callMethod(IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>> next) throws ProcessingException {
          setReturnValue(next.execIsEditable(ColumnIsEditableChain.this, row));
        }
      };
      callChain(methodInvocation, row);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
      return methodInvocation.getReturnValue();
    }
  }

  public static class ColumnValidateValueChain<VALUE> extends AbstractColumnChain<VALUE> {

    public ColumnValidateValueChain(List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions) {
      super(extensions);
    }

    public VALUE execValidateValue(final ITableRow row, final VALUE rawValue) throws ProcessingException {
      MethodInvocation<VALUE> methodInvocation = new MethodInvocation<VALUE>() {
        @Override
        protected void callMethod(IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>> next) throws ProcessingException {
          setReturnValue(next.execValidateValue(ColumnValidateValueChain.this, row, rawValue));
        }
      };
      callChain(methodInvocation, row, rawValue);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
      return methodInvocation.getReturnValue();
    }
  }

  public static class ColumnPrepareEditChain<VALUE> extends AbstractColumnChain<VALUE> {

    public ColumnPrepareEditChain(List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions) {
      super(extensions);
    }

    public IFormField execPrepareEdit(final ITableRow row) throws ProcessingException {
      MethodInvocation<IFormField> methodInvocation = new MethodInvocation<IFormField>() {
        @Override
        protected void callMethod(IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>> next) throws ProcessingException {
          setReturnValue(next.execPrepareEdit(ColumnPrepareEditChain.this, row));
        }
      };
      callChain(methodInvocation, row);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
      return methodInvocation.getReturnValue();
    }
  }

  public static class ColumnDecorateHeaderCellChain<VALUE> extends AbstractColumnChain<VALUE> {

    public ColumnDecorateHeaderCellChain(List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions) {
      super(extensions);
    }

    public void execDecorateHeaderCell(final HeaderCell cell) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>> next) throws ProcessingException {
          next.execDecorateHeaderCell(ColumnDecorateHeaderCellChain.this, cell);
        }
      };
      callChain(methodInvocation, cell);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class ColumnDecorateCellChain<VALUE> extends AbstractColumnChain<VALUE> {

    public ColumnDecorateCellChain(List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions) {
      super(extensions);
    }

    public void execDecorateCell(final Cell cell, final ITableRow row) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>> next) throws ProcessingException {
          next.execDecorateCell(ColumnDecorateCellChain.this, cell, row);
        }
      };
      callChain(methodInvocation, cell, row);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class ColumnDisposeColumnChain<VALUE> extends AbstractColumnChain<VALUE> {

    public ColumnDisposeColumnChain(List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions) {
      super(extensions);
    }

    public void execDisposeColumn() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>> next) throws ProcessingException {
          next.execDisposeColumn(ColumnDisposeColumnChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
