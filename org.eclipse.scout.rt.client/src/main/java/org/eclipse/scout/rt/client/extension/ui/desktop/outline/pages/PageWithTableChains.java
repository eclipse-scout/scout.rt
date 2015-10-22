package org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.ITreeNodeExtension;
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
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

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
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
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
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class PageWithTableCreateVirtualChildPageChain<T extends ITable> extends AbstractPageWithTableChain<T> {

    public PageWithTableCreateVirtualChildPageChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public IPage<?> execCreateVirtualChildPage(final ITableRow row) {
      MethodInvocation<IPage> methodInvocation = new MethodInvocation<IPage>() {
        @Override
        protected void callMethod(IPageWithTableExtension<? extends ITable, ? extends AbstractPageWithTable<? extends ITable>> next) {
          setReturnValue(next.execCreateVirtualChildPage(PageWithTableCreateVirtualChildPageChain.this, row));
        }
      };
      callChain(methodInvocation, row);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
      return methodInvocation.getReturnValue();
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
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
