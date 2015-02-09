package org.eclipse.scout.rt.client.extension.ui.basic.table;

import java.net.URL;
import java.util.List;

import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRowDataMapper;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class TableChains {

  private TableChains() {
  }

  protected abstract static class AbstractTableChain extends AbstractExtensionChain<ITableExtension<? extends AbstractTable>> {

    public AbstractTableChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions, ITableExtension.class);
    }
  }

  public static class TableHyperlinkActionChain extends AbstractTableChain {

    public TableHyperlinkActionChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public void execHyperlinkAction(final URL url, final String path, final boolean local) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) throws ProcessingException {
          next.execHyperlinkAction(TableHyperlinkActionChain.this, url, path, local);
        }
      };
      callChain(methodInvocation, url, path, local);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TableRowActionChain extends AbstractTableChain {

    public TableRowActionChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public void execRowAction(final ITableRow row) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) throws ProcessingException {
          next.execRowAction(TableRowActionChain.this, row);
        }
      };
      callChain(methodInvocation, row);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TableContentChangedChain extends AbstractTableChain {

    public TableContentChangedChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public void execContentChanged() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) throws ProcessingException {
          next.execContentChanged(TableContentChangedChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TableCreateTableRowDataMapperChain extends AbstractTableChain {

    public TableCreateTableRowDataMapperChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public ITableRowDataMapper execCreateTableRowDataMapper(final Class<? extends AbstractTableRowData> rowType) throws ProcessingException {
      MethodInvocation<ITableRowDataMapper> methodInvocation = new MethodInvocation<ITableRowDataMapper>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) throws ProcessingException {
          setReturnValue(next.execCreateTableRowDataMapper(TableCreateTableRowDataMapperChain.this, rowType));
        }
      };
      callChain(methodInvocation, rowType);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
      return methodInvocation.getReturnValue();
    }
  }

  public static class TableInitTableChain extends AbstractTableChain {

    public TableInitTableChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public void execInitTable() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) throws ProcessingException {
          next.execInitTable(TableInitTableChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TableResetColumnsChain extends AbstractTableChain {

    public TableResetColumnsChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public void execResetColumns(final boolean visibility, final boolean order, final boolean sorting, final boolean widths) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) throws ProcessingException {
          next.execResetColumns(TableResetColumnsChain.this, visibility, order, sorting, widths);
        }
      };
      callChain(methodInvocation, visibility, order, sorting, widths);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TableDecorateCellChain extends AbstractTableChain {

    public TableDecorateCellChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public void execDecorateCell(final Cell view, final ITableRow row, final IColumn<?> col) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) throws ProcessingException {
          next.execDecorateCell(TableDecorateCellChain.this, view, row, col);
        }
      };
      callChain(methodInvocation, view, row, col);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TableDropChain extends AbstractTableChain {

    public TableDropChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public void execDrop(final ITableRow row, final TransferObject t) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) throws ProcessingException {
          next.execDrop(TableDropChain.this, row, t);
        }
      };
      callChain(methodInvocation, row, t);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TableDisposeTableChain extends AbstractTableChain {

    public TableDisposeTableChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public void execDisposeTable() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) throws ProcessingException {
          next.execDisposeTable(TableDisposeTableChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TableRowClickChain extends AbstractTableChain {

    public TableRowClickChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public void execRowClick(final ITableRow row, final MouseButton mouseButton) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) throws ProcessingException {
          next.execRowClick(TableRowClickChain.this, row, mouseButton);
        }
      };
      callChain(methodInvocation, row, mouseButton);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TableDecorateRowChain extends AbstractTableChain {

    public TableDecorateRowChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public void execDecorateRow(final ITableRow row) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) throws ProcessingException {
          next.execDecorateRow(TableDecorateRowChain.this, row);
        }
      };
      callChain(methodInvocation, row);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TableCopyChain extends AbstractTableChain {

    public TableCopyChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public TransferObject execCopy(final List<? extends ITableRow> rows) throws ProcessingException {
      MethodInvocation<TransferObject> methodInvocation = new MethodInvocation<TransferObject>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) throws ProcessingException {
          setReturnValue(next.execCopy(TableCopyChain.this, rows));
        }
      };
      callChain(methodInvocation, rows);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
      return methodInvocation.getReturnValue();
    }
  }

  public static class TableRowsSelectedChain extends AbstractTableChain {

    public TableRowsSelectedChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public void execRowsSelected(final List<? extends ITableRow> rows) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) throws ProcessingException {
          next.execRowsSelected(TableRowsSelectedChain.this, rows);
        }
      };
      callChain(methodInvocation, rows);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class TableDragChain extends AbstractTableChain {

    public TableDragChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public TransferObject execDrag(final List<ITableRow> rows) throws ProcessingException {
      MethodInvocation<TransferObject> methodInvocation = new MethodInvocation<TransferObject>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) throws ProcessingException {
          setReturnValue(next.execDrag(TableDragChain.this, rows));
        }
      };
      callChain(methodInvocation, rows);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
      return methodInvocation.getReturnValue();
    }
  }
}
