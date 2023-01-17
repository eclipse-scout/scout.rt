/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.svg.client.extension.svgfield;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;
import org.eclipse.scout.rt.svg.client.svgfield.AbstractSvgField;
import org.eclipse.scout.rt.svg.client.svgfield.SvgFieldEvent;

public final class SvgFieldChains {

  private SvgFieldChains() {
  }

  protected abstract static class AbstractSvgFieldChain extends AbstractExtensionChain<ISvgFieldExtension<? extends AbstractSvgField>> {

    public AbstractSvgFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, ISvgFieldExtension.class);
    }
  }

  public static class SvgFieldClickedChain extends AbstractSvgFieldChain {

    public SvgFieldClickedChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execClicked(final SvgFieldEvent e) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ISvgFieldExtension<? extends AbstractSvgField> next) {
          next.execClicked(SvgFieldClickedChain.this, e);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class SvgFieldAppLinkActionChain extends AbstractSvgFieldChain {

    public SvgFieldAppLinkActionChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execAppLinkAction(final String ref) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ISvgFieldExtension<? extends AbstractSvgField> next) {
          next.execAppLinkAction(SvgFieldAppLinkActionChain.this, ref);
        }
      };
      callChain(methodInvocation);
    }
  }
}
