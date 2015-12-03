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
package org.eclipse.scout.rt.client.extension.ui.form.fields.htmlfield;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class HtmlFieldChains {

  private HtmlFieldChains() {
  }

  protected abstract static class AbstractHtmlFieldChain extends AbstractExtensionChain<IHtmlFieldExtension<? extends AbstractHtmlField>> {

    public AbstractHtmlFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IHtmlFieldExtension.class);
    }
  }

  public static class HtmlFieldAppLinkActionChain extends AbstractHtmlFieldChain {

    public HtmlFieldAppLinkActionChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execAppLinkAction(final String ref) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IHtmlFieldExtension<? extends AbstractHtmlField> next) {
          next.execAppLinkAction(HtmlFieldAppLinkActionChain.this, ref);
        }
      };
      callChain(methodInvocation, ref);
    }
  }
}
