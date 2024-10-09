/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages;

import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class PageChains {

  private PageChains() {
  }

  protected abstract static class AbstractPageChain extends AbstractExtensionChain<IPageExtension<? extends AbstractPage>> {

    public AbstractPageChain(List<? extends IPageExtension<? extends AbstractPage>> extensions) {
      super(extensions, IPageExtension.class);
    }
  }

  public static class PageReloadPageChain extends AbstractPageChain {

    public PageReloadPageChain(List<? extends IPageExtension<? extends AbstractPage>> extensions) {
      super(extensions);
    }

    public void execReloadPage(String reloadReason) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageExtension<? extends AbstractPage> next) {
          next.execReloadPage(PageReloadPageChain.this, reloadReason);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class PagePageDataLoadedChain extends AbstractPageChain {

    public PagePageDataLoadedChain(List<? extends IPageExtension<? extends AbstractPage>> extensions) {
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

    public PagePageActivatedChain(List<? extends IPageExtension<? extends AbstractPage>> extensions) {
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

    public PageDataChangedChain(List<? extends IPageExtension<? extends AbstractPage>> extensions) {
      super(extensions);
    }

    public void execDataChanged(final Object... dataTypes) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageExtension<? extends AbstractPage> next) {
          next.execDataChanged(PageDataChangedChain.this, dataTypes);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class PageInitPageChain extends AbstractPageChain {

    public PageInitPageChain(List<? extends IPageExtension<? extends AbstractPage>> extensions) {
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

    public PageInitDetailFormChain(List<? extends IPageExtension<? extends AbstractPage>> extensions) {
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

    public PagePageDeactivatedChain(List<? extends IPageExtension<? extends AbstractPage>> extensions) {
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

    public PageDisposePageChain(List<? extends IPageExtension<? extends AbstractPage>> extensions) {
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

    public PageInitTableChain(List<? extends IPageExtension<? extends AbstractPage>> extensions) {
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

    public PageDetailFormActivatedChain(List<? extends IPageExtension<? extends AbstractPage>> extensions) {
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

    public PageCalculateVisibleChain(List<? extends IPageExtension<? extends AbstractPage>> extensions) {
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

    public ComputeParentTablePageMenusChain(List<? extends IPageExtension<? extends AbstractPage>> extensions) {
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
