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
package org.eclipse.scout.rt.client.extension.ui.action;

import java.util.List;

import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class ActionChains {

  private ActionChains() {
  }

  protected abstract static class AbstractActionChain extends AbstractExtensionChain<IActionExtension<? extends AbstractAction>> {

    public AbstractActionChain(List<? extends IActionExtension<? extends AbstractAction>> extensions) {
      super(extensions, IActionExtension.class);
    }
  }

  public static class ActionSelectionChangedChain extends AbstractActionChain {

    public ActionSelectionChangedChain(List<? extends IActionExtension<? extends AbstractAction>> extensions) {
      super(extensions);
    }

    public void execSelectionChanged(final boolean selection) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IActionExtension<? extends AbstractAction> next) {
          next.execSelectionChanged(ActionSelectionChangedChain.this, selection);
        }
      };
      callChain(methodInvocation, selection);
    }
  }

  public static class ActionActionChain extends AbstractActionChain {

    public ActionActionChain(List<? extends IActionExtension<? extends AbstractAction>> extensions) {
      super(extensions);
    }

    public void execAction() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IActionExtension<? extends AbstractAction> next) {
          next.execAction(ActionActionChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class ActionInitActionChain extends AbstractActionChain {

    public ActionInitActionChain(List<? extends IActionExtension<? extends AbstractAction>> extensions) {
      super(extensions);
    }

    public void execInitAction() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IActionExtension<? extends AbstractAction> next) {
          next.execInitAction(ActionInitActionChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class ActionDisposeChain extends AbstractActionChain {

    public ActionDisposeChain(List<? extends IActionExtension<? extends AbstractAction>> extensions) {
      super(extensions);
    }

    public void execDispose() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IActionExtension<? extends AbstractAction> next) {
          next.execDispose(ActionDisposeChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }
}
