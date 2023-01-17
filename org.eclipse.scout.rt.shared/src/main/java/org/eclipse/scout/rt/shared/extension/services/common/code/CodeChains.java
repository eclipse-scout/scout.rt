/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.extension.services.common.code;

import java.util.List;

import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCode;
import org.eclipse.scout.rt.shared.services.common.code.ICode;

public final class CodeChains {

  private CodeChains() {
  }

  protected abstract static class AbstractCodeChain<T> extends AbstractExtensionChain<ICodeExtension<T, ? extends AbstractCode<T>>> {

    public AbstractCodeChain(List<? extends ICodeExtension<T, ? extends AbstractCode<T>>> extensions) {
      super(extensions, ICodeExtension.class);
    }
  }

  public static class CodeCreateChildCodesChain<T> extends AbstractCodeChain<T> {

    public CodeCreateChildCodesChain(List<? extends ICodeExtension<T, ? extends AbstractCode<T>>> extensions) {
      super(extensions);
    }

    public List<? extends ICode<T>> execCreateChildCodes() {
      MethodInvocation<List<? extends ICode<T>>> methodInvocation = new MethodInvocation<List<? extends ICode<T>>>() {
        @Override
        protected void callMethod(ICodeExtension<T, ? extends AbstractCode<T>> next) {
          setReturnValue(next.execCreateChildCodes(CodeCreateChildCodesChain.this));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }
}
