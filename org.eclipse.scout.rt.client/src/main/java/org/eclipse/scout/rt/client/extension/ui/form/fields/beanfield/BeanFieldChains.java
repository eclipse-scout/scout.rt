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
      callChain(methodInvocation, ref);
    }
  }
}
