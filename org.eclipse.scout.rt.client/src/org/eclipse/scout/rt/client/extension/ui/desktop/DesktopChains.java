/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

/**
 *
 */
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

    public void execInit() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(org.eclipse.scout.rt.client.extension.ui.desktop.IDesktopExtension<? extends AbstractDesktop> next) throws ProcessingException {
          next.execInit(org.eclipse.scout.rt.client.extension.ui.desktop.DesktopChains.DesktopInitChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class DesktopOpenedChain extends AbstractDesktopChain {

    public DesktopOpenedChain(List<? extends IDesktopExtension<? extends AbstractDesktop>> extensions) {
      super(extensions);
    }

    public void execOpened() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDesktopExtension<? extends AbstractDesktop> next) throws ProcessingException {
          next.execOpened(DesktopOpenedChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class DesktopAddTrayMenusChain extends AbstractDesktopChain {

    public DesktopAddTrayMenusChain(List<? extends IDesktopExtension<? extends AbstractDesktop>> extensions) {
      super(extensions);
    }

    public void execAddTrayMenus(final List<IMenu> menus) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDesktopExtension<? extends AbstractDesktop> next) throws ProcessingException {
          next.execAddTrayMenus(DesktopAddTrayMenusChain.this, menus);
        }
      };
      callChain(methodInvocation, menus);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class DesktopBeforeClosingChain extends AbstractDesktopChain {

    public DesktopBeforeClosingChain(List<? extends IDesktopExtension<? extends AbstractDesktop>> extensions) {
      super(extensions);
    }

    public void execBeforeClosing() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDesktopExtension<? extends AbstractDesktop> next) throws ProcessingException {
          next.execBeforeClosing(DesktopBeforeClosingChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class DesktopPageDetailFormChangedChain extends AbstractDesktopChain {

    public DesktopPageDetailFormChangedChain(List<? extends IDesktopExtension<? extends AbstractDesktop>> extensions) {
      super(extensions);
    }

    public void execPageDetailFormChanged(final IForm oldForm, final IForm newForm) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDesktopExtension<? extends AbstractDesktop> next) throws ProcessingException {
          next.execPageDetailFormChanged(DesktopPageDetailFormChangedChain.this, oldForm, newForm);
        }
      };
      callChain(methodInvocation, oldForm, newForm);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class DesktopTablePageLoadedChain extends AbstractDesktopChain {

    public DesktopTablePageLoadedChain(List<? extends IDesktopExtension<? extends AbstractDesktop>> extensions) {
      super(extensions);
    }

    public void execTablePageLoaded(final IPageWithTable<?> tablePage) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDesktopExtension<? extends AbstractDesktop> next) throws ProcessingException {
          next.execTablePageLoaded(DesktopTablePageLoadedChain.this, tablePage);
        }
      };
      callChain(methodInvocation, tablePage);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class DesktopOutlineChangedChain extends AbstractDesktopChain {

    public DesktopOutlineChangedChain(List<? extends IDesktopExtension<? extends AbstractDesktop>> extensions) {
      super(extensions);
    }

    public void execOutlineChanged(final IOutline oldOutline, final IOutline newOutline) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDesktopExtension<? extends AbstractDesktop> next) throws ProcessingException {
          next.execOutlineChanged(DesktopOutlineChangedChain.this, oldOutline, newOutline);
        }
      };
      callChain(methodInvocation, oldOutline, newOutline);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class DesktopClosingChain extends AbstractDesktopChain {

    public DesktopClosingChain(List<? extends IDesktopExtension<? extends AbstractDesktop>> extensions) {
      super(extensions);
    }

    public void execClosing() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDesktopExtension<? extends AbstractDesktop> next) throws ProcessingException {
          next.execClosing(DesktopClosingChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class DesktopPageSearchFormChangedChain extends AbstractDesktopChain {

    public DesktopPageSearchFormChangedChain(List<? extends IDesktopExtension<? extends AbstractDesktop>> extensions) {
      super(extensions);
    }

    public void execPageSearchFormChanged(final IForm oldForm, final IForm newForm) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDesktopExtension<? extends AbstractDesktop> next) throws ProcessingException {
          next.execPageSearchFormChanged(DesktopPageSearchFormChangedChain.this, oldForm, newForm);
        }
      };
      callChain(methodInvocation, oldForm, newForm);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class DesktopPageDetailTableChangedChain extends AbstractDesktopChain {

    public DesktopPageDetailTableChangedChain(List<? extends IDesktopExtension<? extends AbstractDesktop>> extensions) {
      super(extensions);
    }

    public void execPageDetailTableChanged(final ITable oldTable, final ITable newTable) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDesktopExtension<? extends AbstractDesktop> next) throws ProcessingException {
          next.execPageDetailTableChanged(DesktopPageDetailTableChangedChain.this, oldTable, newTable);
        }
      };
      callChain(methodInvocation, oldTable, newTable);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class DesktopGuiAttachedChain extends AbstractDesktopChain {

    public DesktopGuiAttachedChain(List<? extends IDesktopExtension<? extends AbstractDesktop>> extensions) {
      super(extensions);
    }

    public void execGuiAttached() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDesktopExtension<? extends AbstractDesktop> next) throws ProcessingException {
          next.execGuiAttached(DesktopGuiAttachedChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class DesktopGuiDetachedChain extends AbstractDesktopChain {

    public DesktopGuiDetachedChain(List<? extends IDesktopExtension<? extends AbstractDesktop>> extensions) {
      super(extensions);
    }

    public void execGuiDetached() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDesktopExtension<? extends AbstractDesktop> next) throws ProcessingException {
          next.execGuiDetached(DesktopGuiDetachedChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
