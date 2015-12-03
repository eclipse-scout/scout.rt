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
package org.eclipse.scout.rt.client.extension.ui.action.menu;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.action.IActionExtension;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class MenuChains {

  private MenuChains() {
  }

  protected abstract static class AbstractMenuChain extends AbstractExtensionChain<IMenuExtension<? extends AbstractMenu>> {

    public AbstractMenuChain(List<? extends IActionExtension<? extends AbstractAction>> extensions) {
      super(extensions, IMenuExtension.class);
    }
  }

  public static class MenuOwnerValueChangedChain extends AbstractMenuChain {

    public MenuOwnerValueChangedChain(List<? extends IActionExtension<? extends AbstractAction>> extensions) {
      super(extensions);
    }

    public void execOwnerValueChanged(final Object newOwnerValue) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IMenuExtension<? extends AbstractMenu> next) {
          next.execOwnerValueChanged(MenuOwnerValueChangedChain.this, newOwnerValue);
        }
      };
      callChain(methodInvocation, newOwnerValue);
    }
  }
}
