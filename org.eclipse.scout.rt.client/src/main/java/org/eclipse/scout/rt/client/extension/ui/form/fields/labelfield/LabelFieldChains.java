/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.Label
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
      callChain(methodInvocation, ref);
    }
  }
}
