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
package org.eclipse.scout.rt.client.extension.ui.form.fields.datefield;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class DateFieldChains {

  private DateFieldChains() {
  }

  protected abstract static class AbstractDateFieldChain extends AbstractExtensionChain<IDateFieldExtension<? extends AbstractDateField>> {

    public AbstractDateFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IDateFieldExtension.class);
    }
  }

  public static class DateFieldShiftTimeChain extends AbstractDateFieldChain {

    public DateFieldShiftTimeChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execShiftTime(final int level, final int value) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDateFieldExtension<? extends AbstractDateField> next) {
          next.execShiftTime(DateFieldShiftTimeChain.this, level, value);
        }
      };
      callChain(methodInvocation, level, value);
    }
  }

  public static class DateFieldShiftDateChain extends AbstractDateFieldChain {

    public DateFieldShiftDateChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execShiftDate(final int level, final int value) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IDateFieldExtension<? extends AbstractDateField> next) {
          next.execShiftDate(DateFieldShiftDateChain.this, level, value);
        }
      };
      callChain(methodInvocation, level, value);
    }
  }
}
