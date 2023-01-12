/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.beanfield;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.beanfield.AbstractBeanField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class BeanFieldChains {

  private BeanFieldChains() {
  }

  protected abstract static class AbstractBeanFieldChain<VALUE> extends AbstractExtensionChain<IBeanFieldExtension<VALUE, ? extends AbstractBeanField<VALUE>>> {

    public AbstractBeanFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IBeanFieldExtension.class);
    }
  }

  public static class BeanFieldAppLinkActionChain<VALUE> extends AbstractBeanFieldChain<VALUE> {

    public BeanFieldAppLinkActionChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execAppLinkAction(final String ref) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IBeanFieldExtension<VALUE, ? extends AbstractBeanField<VALUE>> next) {
          next.execAppLinkAction(BeanFieldAppLinkActionChain.this, ref);
        }
      };
      callChain(methodInvocation);
    }
  }
}
