/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.accordionfield;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.accordion.IAccordion;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.accordionfield.AbstractAccordionField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class AccordionFieldChains {

  private AccordionFieldChains() {
  }

  protected abstract static class AbstractAccordionFieldChain<T extends IAccordion> extends AbstractExtensionChain<IAccordionFieldExtension<T, ? extends AbstractAccordionField>> {

    public AbstractAccordionFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IAccordionFieldExtension.class);
    }
  }

  public static class AccordionFieldDragRequestChain<T extends IAccordion> extends AbstractAccordionFieldChain<T> {

    public AccordionFieldDragRequestChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public TransferObject execDragRequest() {
      MethodInvocation<TransferObject> methodInvocation = new MethodInvocation<TransferObject>() {
        @Override
        protected void callMethod(IAccordionFieldExtension<T, ? extends AbstractAccordionField> next) {
          setReturnValue(next.execDragRequest(AccordionFieldDragRequestChain.this));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }

  public static class AccordionFieldDropRequestChain<T extends IAccordion> extends AbstractAccordionFieldChain<T> {

    public AccordionFieldDropRequestChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execDropRequest(final TransferObject transferObject) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IAccordionFieldExtension<T, ? extends AbstractAccordionField> next) {
          next.execDropRequest(AccordionFieldDropRequestChain.this, transferObject);
        }
      };
      callChain(methodInvocation, transferObject);
    }
  }
}
