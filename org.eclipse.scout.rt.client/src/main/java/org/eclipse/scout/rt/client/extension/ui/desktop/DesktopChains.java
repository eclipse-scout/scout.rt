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
package org.eclipse.scout.rt.client.extension.ui.desktop;

import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class DesktopChains {

  private DesktopChains() {
  }

  protected abstract static class AbstractDesktopChain extends AbstractExtensionChain<org.eclipse.scout.rt.client.extension.ui.desktop.IDesktopExtension<? extends AbstractDesktop>> {

    public AbstractDesktopChain(List<? extends org.eclipse.scout.rt.client.extension.ui.desktop.IDesktopExtension<? extends AbstractDesktop>> extensions) {
      super(extensions, IDesktopExtension.class);
    }
  }

  public static class DesktopInitChain extends org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.AbstractDesktopChain {

    public DesktopInitChain(List<? extends org.eclipse.scout.rt.client.extension.ui.desktop.IDesktopExtension<? extends AbstractDesktop>> extensions) {
      super(extensions);
    }

    public void execInit() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(org.eclipse.scout.rt.client.extension.ui.desktop.IDesktopExtension<? extends AbstractDesktop> next) {
          next.execInit(org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopInitChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class DesktopOpenedChain extends AbstractDesktopChain {

    public DesktopOpenedChain(List<? extends IDesktopExtension<? extends AbstractDesktop>> extensions) {
      super(extensions);
    }

    public void execOpened() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDesktopExtension<? extends AbstractDesktop> next) {
          next.execOpened(DesktopOpenedChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class DesktopAddTrayMenusChain extends AbstractDesktopChain {

    public DesktopAddTrayMenusChain(List<? extends IDesktopExtension<? extends AbstractDesktop>> extensions) {
      super(extensions);
    }

    public void execAddTrayMenus(final List<IMenu> menus) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDesktopExtension<? extends AbstractDesktop> next) {
          next.execAddTrayMenus(DesktopAddTrayMenusChain.this, menus);
        }
      };
      callChain(methodInvocation, menus);
    }
  }

  public static class DesktopBeforeClosingChain extends AbstractDesktopChain {

    public DesktopBeforeClosingChain(List<? extends IDesktopExtension<? extends AbstractDesktop>> extensions) {
      super(extensions);
    }

    public void execBeforeClosing() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDesktopExtension<? extends AbstractDesktop> next) {
          next.execBeforeClosing(DesktopBeforeClosingChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class DesktopPageDetailFormChangedChain extends AbstractDesktopChain {

    public DesktopPageDetailFormChangedChain(List<? extends IDesktopExtension<? extends AbstractDesktop>> extensions) {
      super(extensions);
    }

    public void execPageDetailFormChanged(final IForm oldForm, final IForm newForm) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDesktopExtension<? extends AbstractDesktop> next) {
          next.execPageDetailFormChanged(DesktopPageDetailFormChangedChain.this, oldForm, newForm);
        }
      };
      callChain(methodInvocation, oldForm, newForm);
    }
  }

  public static class DesktopTablePageLoadedChain extends AbstractDesktopChain {

    public DesktopTablePageLoadedChain(List<? extends IDesktopExtension<? extends AbstractDesktop>> extensions) {
      super(extensions);
    }

    public void execTablePageLoaded(final IPageWithTable<?> tablePage) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDesktopExtension<? extends AbstractDesktop> next) {
          next.execTablePageLoaded(DesktopTablePageLoadedChain.this, tablePage);
        }
      };
      callChain(methodInvocation, tablePage);
    }
  }

  public static class DesktopOutlineChangedChain extends AbstractDesktopChain {

    public DesktopOutlineChangedChain(List<? extends IDesktopExtension<? extends AbstractDesktop>> extensions) {
      super(extensions);
    }

    public void execOutlineChanged(final IOutline oldOutline, final IOutline newOutline) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDesktopExtension<? extends AbstractDesktop> next) {
          next.execOutlineChanged(DesktopOutlineChangedChain.this, oldOutline, newOutline);
        }
      };
      callChain(methodInvocation, oldOutline, newOutline);
    }
  }

  public static class DesktopFormAboutToShowChain extends AbstractDesktopChain {

    public DesktopFormAboutToShowChain(List<? extends IDesktopExtension<? extends AbstractDesktop>> extensions) {
      super(extensions);
    }

    public IForm execFormAboutToShow(final IForm form) {
      MethodInvocation<IForm> methodInvocation = new MethodInvocation<IForm>() {
        @Override
        protected void callMethod(IDesktopExtension<? extends AbstractDesktop> next) {
          setReturnValue(next.execFormAboutToShow(DesktopFormAboutToShowChain.this, form));
        }
      };
      callChain(methodInvocation, form);
      return methodInvocation.getReturnValue();
    }
  }

  public static class DesktopClosingChain extends AbstractDesktopChain {

    public DesktopClosingChain(List<? extends IDesktopExtension<? extends AbstractDesktop>> extensions) {
      super(extensions);
    }

    public void execClosing() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDesktopExtension<? extends AbstractDesktop> next) {
          next.execClosing(DesktopClosingChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class DesktopPageSearchFormChangedChain extends AbstractDesktopChain {

    public DesktopPageSearchFormChangedChain(List<? extends IDesktopExtension<? extends AbstractDesktop>> extensions) {
      super(extensions);
    }

    public void execPageSearchFormChanged(final IForm oldForm, final IForm newForm) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDesktopExtension<? extends AbstractDesktop> next) {
          next.execPageSearchFormChanged(DesktopPageSearchFormChangedChain.this, oldForm, newForm);
        }
      };
      callChain(methodInvocation, oldForm, newForm);
    }
  }

  public static class DesktopPageDetailTableChangedChain extends AbstractDesktopChain {

    public DesktopPageDetailTableChangedChain(List<? extends IDesktopExtension<? extends AbstractDesktop>> extensions) {
      super(extensions);
    }

    public void execPageDetailTableChanged(final ITable oldTable, final ITable newTable) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDesktopExtension<? extends AbstractDesktop> next) {
          next.execPageDetailTableChanged(DesktopPageDetailTableChangedChain.this, oldTable, newTable);
        }
      };
      callChain(methodInvocation, oldTable, newTable);
    }
  }

  public static class DesktopGuiAttachedChain extends AbstractDesktopChain {

    public DesktopGuiAttachedChain(List<? extends IDesktopExtension<? extends AbstractDesktop>> extensions) {
      super(extensions);
    }

    public void execGuiAttached(final String pathInfo) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDesktopExtension<? extends AbstractDesktop> next) {
          next.execGuiAttached(DesktopGuiAttachedChain.this, pathInfo);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class DesktopGuiDetachedChain extends AbstractDesktopChain {

    public DesktopGuiDetachedChain(List<? extends IDesktopExtension<? extends AbstractDesktop>> extensions) {
      super(extensions);
    }

    public void execGuiDetached() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDesktopExtension<? extends AbstractDesktop> next) {
          next.execGuiDetached(DesktopGuiDetachedChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }
}
