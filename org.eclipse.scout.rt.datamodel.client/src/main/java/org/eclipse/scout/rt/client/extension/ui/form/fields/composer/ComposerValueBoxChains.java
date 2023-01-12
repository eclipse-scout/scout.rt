/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.composer;

import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractComposerValueBox;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerValueField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class ComposerValueBoxChains {

  private ComposerValueBoxChains() {
  }

  protected abstract static class AbstractComposerValueBoxChain extends AbstractExtensionChain<IComposerValueBoxExtension<? extends AbstractComposerValueBox>> {

    public AbstractComposerValueBoxChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IComposerValueBoxExtension.class);
    }
  }

  public static class ComposerValueBoxChangedValueChain extends AbstractComposerValueBoxChain {

    public ComposerValueBoxChangedValueChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execChangedValue() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IComposerValueBoxExtension<? extends AbstractComposerValueBox> next) {
          next.execChangedValue(ComposerValueBoxChangedValueChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class ComposerValueBoxInitOperatorToFieldMapChain extends AbstractComposerValueBoxChain {

    public ComposerValueBoxInitOperatorToFieldMapChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execInitOperatorToFieldMap(final Map<Integer /* operator */, Map<Integer /* field type */, IComposerValueField>> operatorTypeToFieldMap) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IComposerValueBoxExtension<? extends AbstractComposerValueBox> next) {
          next.execInitOperatorToFieldMap(ComposerValueBoxInitOperatorToFieldMapChain.this, operatorTypeToFieldMap);
        }
      };
      callChain(methodInvocation);
    }
  }
}
