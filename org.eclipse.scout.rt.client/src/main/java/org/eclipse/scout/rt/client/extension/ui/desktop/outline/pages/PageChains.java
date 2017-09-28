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
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class PageChains {

  private PageChains() {
  }

  protected abstract static class AbstractPageChain extends AbstractExtensionChain<IPageExtension<? extends AbstractPage>> {

    public AbstractPageChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions, IPageExtension.class);
    }
  }

  public static class PagePageDataLoadedChain extends AbstractPageChain {

    public PagePageDataLoadedChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execPageDataLoaded() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageExtension<? extends AbstractPage> next) {
          next.execPageDataLoaded(PagePageDataLoadedChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class PagePageActivatedChain extends AbstractPageChain {

    public PagePageActivatedChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execPageActivated() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageExtension<? extends AbstractPage> next) {
          next.execPageActivated(PagePageActivatedChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class PageDataChangedChain extends AbstractPageChain {

    public PageDataChangedChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execDataChanged(final Object... dataTypes) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageExtension<? extends AbstractPage> next) {
          next.execDataChanged(PageDataChangedChain.this, dataTypes);
        }
      };
      callChain(methodInvocation, dataTypes);
    }
  }

  public static class PageInitPageChain extends AbstractPageChain {

    public PageInitPageChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execInitPage() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageExtension<? extends AbstractPage> next) {
          next.execInitPage(PageInitPageChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class PageInitDetailFormChain extends AbstractPageChain {

    public PageInitDetailFormChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execInitDetailForm() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageExtension<? extends AbstractPage> next) {
          next.execInitDetailForm(PageInitDetailFormChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class PagePageDeactivatedChain extends AbstractPageChain {

    public PagePageDeactivatedChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execPageDeactivated() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageExtension<? extends AbstractPage> next) {
          next.execPageDeactivated(PagePageDeactivatedChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class PageDisposePageChain extends AbstractPageChain {

    public PageDisposePageChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execDisposePage() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageExtension<? extends AbstractPage> next) {
          next.execDisposePage(PageDisposePageChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class PageInitTableChain extends AbstractPageChain {

    public PageInitTableChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execInitTable() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageExtension<? extends AbstractPage> next) {
          next.execInitTable(PageInitTableChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class PageDetailFormActivatedChain extends AbstractPageChain {

    public PageDetailFormActivatedChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execDetailFormActivated() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageExtension<? extends AbstractPage> next) {
          next.execDetailFormActivated(PageDetailFormActivatedChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class PageCalculateVisibleChain extends AbstractPageChain {

    public PageCalculateVisibleChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public boolean execCalculateVisible() {
      MethodInvocation<Boolean> methodInvocation = new MethodInvocation<Boolean>() {
        @Override
        protected void callMethod(IPageExtension<? extends AbstractPage> next) {
          setReturnValue(next.execCalculateVisible(PageCalculateVisibleChain.this));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }

  public static class ComputeParentTablePageMenusChain extends AbstractPageChain {

    public ComputeParentTablePageMenusChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public List<IMenu> execComputeParentTablePageMenus(final IPageWithTable<?> parentTablePage) {
      MethodInvocation<List<IMenu>> methodInvocation = new MethodInvocation<List<IMenu>>() {
        @Override
        protected void callMethod(IPageExtension<? extends AbstractPage> next) {
          setReturnValue(next.execComputeParentTablePageMenus(ComputeParentTablePageMenusChain.this, parentTablePage));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }
}
