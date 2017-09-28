/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.form.fields;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.AbstractBasicField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class BasicFieldChains {

  private BasicFieldChains() {

  }

  public static class BasicFieldExecChangedDisplayTextChain<VALUE> extends AbstractBasicFieldChain<VALUE> {

    /**
     * @param extensions
     *          the list of all extension sorted reverse considering the execution order. The list must be immutable.
     */
    public BasicFieldExecChangedDisplayTextChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execChangedDisplayText() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IBasicFieldExtension<VALUE, ? extends AbstractBasicField<VALUE>> next) {
          next.execChangedDisplayText(BasicFieldExecChangedDisplayTextChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public abstract static class AbstractBasicFieldChain<VALUE> extends AbstractExtensionChain<IBasicFieldExtension<VALUE, ? extends AbstractBasicField<VALUE>>> {

    /**
     * @param executers
     */
    public AbstractBasicFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> executers) {
      super(executers, IBasicFieldExtension.class);
    }
  }
}
