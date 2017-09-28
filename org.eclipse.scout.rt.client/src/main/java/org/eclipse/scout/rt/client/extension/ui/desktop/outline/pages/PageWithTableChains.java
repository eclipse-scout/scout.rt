/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.basic.tree.ITreeNodeExtension;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

public final class PageWithTableChains {

  private PageWithTableChains() {
  }

  protected abstract static class AbstractPageWithTableChain<T extends ITable> extends AbstractExtensionChain<IPageWithTableExtension<? extends ITable, ? extends AbstractPageWithTable<? extends ITable>>> {

    public AbstractPageWithTableChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions, IPageWithTableExtension.class);
    }
  }

  public static class PageWithTableLoadDataChain<T extends ITable> extends AbstractPageWithTableChain<T> {

    public PageWithTableLoadDataChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execLoadData(final SearchFilter filter) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageWithTableExtension<? extends ITable, ? extends AbstractPageWithTable<? extends ITable>> next) {
          next.execLoadData(PageWithTableLoadDataChain.this, filter);
        }
      };
      callChain(methodInvocation, filter);
    }
  }

  public static class PageWithTableCreateChildPageChain<T extends ITable> extends AbstractPageWithTableChain<T> {

    public PageWithTableCreateChildPageChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public IPage<?> execCreateChildPage(final ITableRow row) {
      MethodInvocation<IPage> methodInvocation = new MethodInvocation<IPage>() {
        @Override
        protected void callMethod(IPageWithTableExtension<? extends ITable, ? extends AbstractPageWithTable<? extends ITable>> next) {
          setReturnValue(next.execCreateChildPage(PageWithTableCreateChildPageChain.this, row));
        }
      };
      callChain(methodInvocation, row);
      return methodInvocation.getReturnValue();
    }
  }

  public static class PageWithTablePopulateTableChain<T extends ITable> extends AbstractPageWithTableChain<T> {

    public PageWithTablePopulateTableChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execPopulateTable() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageWithTableExtension<? extends ITable, ? extends AbstractPageWithTable<? extends ITable>> next) {
          next.execPopulateTable(PageWithTablePopulateTableChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class PageWithTableInitSearchFormChain<T extends ITable> extends AbstractPageWithTableChain<T> {

    public PageWithTableInitSearchFormChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execInitSearchForm() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageWithTableExtension<? extends ITable, ? extends AbstractPageWithTable<? extends ITable>> next) {
          next.execInitSearchForm(PageWithTableInitSearchFormChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class PageWithTableComputeTableEmptySpaceMenusChain<T extends ITable> extends AbstractPageWithTableChain<T> {

    public PageWithTableComputeTableEmptySpaceMenusChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public List<IMenu> execComputeTableEmptySpaceMenus() {
      MethodInvocation<List<IMenu>> methodInvocation = new MethodInvocation<List<IMenu>>() {
        @Override
        protected void callMethod(IPageWithTableExtension<? extends ITable, ? extends AbstractPageWithTable<? extends ITable>> next) {
          setReturnValue(next.execComputeTableEmptySpaceMenus(PageWithTableComputeTableEmptySpaceMenusChain.this));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }
}
