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
      callChain(methodInvocation, e);
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
      callChain(methodInvocation, ref);
    }
  }
}
