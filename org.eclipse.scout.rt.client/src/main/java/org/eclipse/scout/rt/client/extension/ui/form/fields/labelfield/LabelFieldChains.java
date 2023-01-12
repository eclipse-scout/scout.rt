/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.labelfield;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.AbstractLabelField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class LabelFieldChains {

  private LabelFieldChains() {
  }

  protected abstract static class AbstractLabelFieldChain extends AbstractExtensionChain<ILabelFieldExtension<? extends AbstractLabelField>> {

    public AbstractLabelFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, ILabelFieldExtension.class);
    }
  }

  public static class LabelFieldAppLinkActionChain extends AbstractLabelFieldChain {

    public LabelFieldAppLinkActionChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execAppLinkAction(final String ref) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ILabelFieldExtension<? extends AbstractLabelField> next) {
          next.execAppLinkAction(LabelFieldAppLinkActionChain.this, ref);
        }
      };
      callChain(methodInvocation);
    }
  }
}
