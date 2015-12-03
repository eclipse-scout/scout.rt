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
package org.eclipse.scout.rt.client.extension.ui.basic.table.columns;

import java.util.List;

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

    public void execCompleteEdit(final ITableRow row, final IFormField editingField) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>> next) {
          next.execCompleteEdit(ColumnCompleteEditChain.this, row, editingField);
        }
      };
      callChain(methodInvocation, row, editingField);
    }
  }

  public static class ColumnInitColumnChain<VALUE> extends AbstractColumnChain<VALUE> {

    public ColumnInitColumnChain(List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions) {
      super(extensions);
    }

    public void execInitColumn() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>> next) {
          next.execInitColumn(ColumnInitColumnChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class ColumnParseValueChain<VALUE> extends AbstractColumnChain<VALUE> {

    public ColumnParseValueChain(List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions) {
      super(extensions);
    }

    public VALUE execParseValue(final ITableRow row, final Object rawValue) {
      MethodInvocation<VALUE> methodInvocation = new MethodInvocation<VALUE>() {
        @Override
        protected void callMethod(IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>> next) {
          setReturnValue(next.execParseValue(ColumnParseValueChain.this, row, rawValue));
        }
      };
      callChain(methodInvocation, row, rawValue);
      return methodInvocation.getReturnValue();
    }
  }

  public static class ColumnValidateValueChain<VALUE> extends AbstractColumnChain<VALUE> {

    public ColumnValidateValueChain(List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions) {
      super(extensions);
    }

    public VALUE execValidateValue(final ITableRow row, final VALUE rawValue) {
      MethodInvocation<VALUE> methodInvocation = new MethodInvocation<VALUE>() {
        @Override
        protected void callMethod(IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>> next) {
          setReturnValue(next.execValidateValue(ColumnValidateValueChain.this, row, rawValue));
        }
      };
      callChain(methodInvocation, row, rawValue);
      return methodInvocation.getReturnValue();
    }
  }

  public static class ColumnPrepareEditChain<VALUE> extends AbstractColumnChain<VALUE> {

    public ColumnPrepareEditChain(List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions) {
      super(extensions);
    }

    public IFormField execPrepareEdit(final ITableRow row) {
      MethodInvocation<IFormField> methodInvocation = new MethodInvocation<IFormField>() {
        @Override
        protected void callMethod(IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>> next) {
          setReturnValue(next.execPrepareEdit(ColumnPrepareEditChain.this, row));
        }
      };
      callChain(methodInvocation, row);
      return methodInvocation.getReturnValue();
    }
  }

  public static class ColumnDecorateHeaderCellChain<VALUE> extends AbstractColumnChain<VALUE> {

    public ColumnDecorateHeaderCellChain(List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions) {
      super(extensions);
    }

    public void execDecorateHeaderCell(final HeaderCell cell) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>> next) {
          next.execDecorateHeaderCell(ColumnDecorateHeaderCellChain.this, cell);
        }
      };
      callChain(methodInvocation, cell);
    }
  }

  public static class ColumnDecorateCellChain<VALUE> extends AbstractColumnChain<VALUE> {

    public ColumnDecorateCellChain(List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions) {
      super(extensions);
    }

    public void execDecorateCell(final Cell cell, final ITableRow row) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>> next) {
          next.execDecorateCell(ColumnDecorateCellChain.this, cell, row);
        }
      };
      callChain(methodInvocation, cell, row);
    }
  }

  public static class ColumnDisposeColumnChain<VALUE> extends AbstractColumnChain<VALUE> {

    public ColumnDisposeColumnChain(List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions) {
      super(extensions);
    }

    public void execDisposeColumn() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>> next) {
          next.execDisposeColumn(ColumnDisposeColumnChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }
}
