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
package org.eclipse.scout.rt.client.extension.ui.form.fields.browserfield;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.AbstractBrowserField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class BrowserFieldChains {

  private BrowserFieldChains() {
  }

  protected abstract static class AbstractBrowserFieldChain extends AbstractExtensionChain<IBrowserFieldExtension<? extends AbstractBrowserField>> {

    public AbstractBrowserFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IBrowserFieldExtension.class);
    }
  }

  public static class BrowserFieldPostMessageChain extends AbstractBrowserFieldChain {

    public BrowserFieldPostMessageChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPostMessage(final String data, final String origin) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IBrowserFieldExtension<? extends AbstractBrowserField> next) {
          next.execPostMessage(BrowserFieldPostMessageChain.this, data, origin);
        }
      };
      callChain(methodInvocation, data, origin);
    }
  }

  public static class BrowserFieldExternalWindowStateChangedChain extends AbstractBrowserFieldChain {

    public BrowserFieldExternalWindowStateChangedChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execExternalWindowStateChanged(final boolean windowState) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IBrowserFieldExtension<? extends AbstractBrowserField> next) {
          next.execExternalWindowStateChanged(BrowserFieldExternalWindowStateChangedChain.this, windowState);
        }
      };
      callChain(methodInvocation, windowState);
    }
  }
}
