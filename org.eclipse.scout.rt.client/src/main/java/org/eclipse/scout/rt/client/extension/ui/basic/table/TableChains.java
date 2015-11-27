package org.eclipse.scout.rt.client.extension.ui.basic.table;

import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRowDataMapper;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
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

  public static class TableAppLinkActionChain extends AbstractTableChain {

    public TableAppLinkActionChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public void execAppLinkAction(final String ref) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) {
          next.execAppLinkAction(TableAppLinkActionChain.this, ref);
        }
      };
      callChain(methodInvocation, ref);
    }
  }

  public static class TableRowActionChain extends AbstractTableChain {

    public TableRowActionChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public void execRowAction(final ITableRow row) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) {
          next.execRowAction(TableRowActionChain.this, row);
        }
      };
      callChain(methodInvocation, row);
    }
  }

  public static class TableContentChangedChain extends AbstractTableChain {

    public TableContentChangedChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public void execContentChanged() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) {
          next.execContentChanged(TableContentChangedChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class TableCreateTableRowDataMapperChain extends AbstractTableChain {

    public TableCreateTableRowDataMapperChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public ITableRowDataMapper execCreateTableRowDataMapper(final Class<? extends AbstractTableRowData> rowType) {
      MethodInvocation<ITableRowDataMapper> methodInvocation = new MethodInvocation<ITableRowDataMapper>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) {
          setReturnValue(next.execCreateTableRowDataMapper(TableCreateTableRowDataMapperChain.this, rowType));
        }
      };
      callChain(methodInvocation, rowType);
      return methodInvocation.getReturnValue();
    }
  }

  public static class TableInitTableChain extends AbstractTableChain {

    public TableInitTableChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public void execInitTable() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) {
          next.execInitTable(TableInitTableChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class TableResetColumnsChain extends AbstractTableChain {

    public TableResetColumnsChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public void execResetColumns(final Set<String> options) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) {
          next.execResetColumns(TableResetColumnsChain.this, options);
        }
      };
      callChain(methodInvocation, options);
    }
  }

  public static class TableDecorateCellChain extends AbstractTableChain {

    public TableDecorateCellChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public void execDecorateCell(final Cell view, final ITableRow row, final IColumn<?> col) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) {
          next.execDecorateCell(TableDecorateCellChain.this, view, row, col);
        }
      };
      callChain(methodInvocation, view, row, col);
    }
  }

  public static class TableDropChain extends AbstractTableChain {

    public TableDropChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public void execDrop(final ITableRow row, final TransferObject t) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) {
          next.execDrop(TableDropChain.this, row, t);
        }
      };
      callChain(methodInvocation, row, t);
    }
  }

  public static class TableDisposeTableChain extends AbstractTableChain {

    public TableDisposeTableChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public void execDisposeTable() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) {
          next.execDisposeTable(TableDisposeTableChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class TableRowClickChain extends AbstractTableChain {

    public TableRowClickChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public void execRowClick(final ITableRow row, final MouseButton mouseButton) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) {
          next.execRowClick(TableRowClickChain.this, row, mouseButton);
        }
      };
      callChain(methodInvocation, row, mouseButton);
    }
  }

  public static class TableRowsCheckedChain extends AbstractTableChain {

    public TableRowsCheckedChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public void execRowsChecked(final List<? extends ITableRow> rows) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) {
          next.execRowsChecked(TableRowsCheckedChain.this, rows);
        }
      };
      callChain(methodInvocation, rows);
    }
  }

  public static class TableDecorateRowChain extends AbstractTableChain {

    public TableDecorateRowChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public void execDecorateRow(final ITableRow row) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) {
          next.execDecorateRow(TableDecorateRowChain.this, row);
        }
      };
      callChain(methodInvocation, row);
    }
  }

  public static class TableCopyChain extends AbstractTableChain {

    public TableCopyChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public TransferObject execCopy(final List<? extends ITableRow> rows) {
      MethodInvocation<TransferObject> methodInvocation = new MethodInvocation<TransferObject>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) {
          setReturnValue(next.execCopy(TableCopyChain.this, rows));
        }
      };
      callChain(methodInvocation, rows);
      return methodInvocation.getReturnValue();
    }
  }

  public static class TableRowsSelectedChain extends AbstractTableChain {

    public TableRowsSelectedChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public void execRowsSelected(final List<? extends ITableRow> rows) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) {
          next.execRowsSelected(TableRowsSelectedChain.this, rows);
        }
      };
      callChain(methodInvocation, rows);
    }
  }

  public static class TableDragChain extends AbstractTableChain {

    public TableDragChain(List<? extends ITableExtension<? extends AbstractTable>> extensions) {
      super(extensions);
    }

    public TransferObject execDrag(final List<ITableRow> rows) {
      MethodInvocation<TransferObject> methodInvocation = new MethodInvocation<TransferObject>() {
        @Override
        protected void callMethod(ITableExtension<? extends AbstractTable> next) {
          setReturnValue(next.execDrag(TableDragChain.this, rows));
        }
      };
      callChain(methodInvocation, rows);
      return methodInvocation.getReturnValue();
    }
  }
}
