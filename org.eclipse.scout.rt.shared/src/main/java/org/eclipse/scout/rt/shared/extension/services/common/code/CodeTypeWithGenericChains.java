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
package org.eclipse.scout.rt.shared.extension.services.common.code;

import java.util.List;

import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeTypeWithGeneric;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.common.code.ICodeRow;

public final class CodeTypeWithGenericChains {

  private CodeTypeWithGenericChains() {
  }

  protected abstract static class AbstractCodeTypeWithGenericChain<CODE_TYPE_ID, CODE_ID, CODE extends ICode<CODE_ID>>
      extends AbstractExtensionChain<ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, ? extends ICode<CODE_ID>>>> {

    public AbstractCodeTypeWithGenericChain(List<? extends ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, ? extends ICode<CODE_ID>>>> extensions) {
      super(extensions, ICodeTypeExtension.class);
    }
  }

  /**
   * chain for extending {@link AbstractCodeTypeWithGeneric#execCreateCodes}
   *
   * @param <CODE_TYPE_ID>
   * @param <CODE_ID>
   * @param <CODE>
   */
  public static class CodeTypeWithGenericCreateCodesChain<CODE_TYPE_ID, CODE_ID, CODE extends ICode<CODE_ID>> extends AbstractCodeTypeWithGenericChain<CODE_TYPE_ID, CODE_ID, CODE> {

    public CodeTypeWithGenericCreateCodesChain(List<? extends ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, ? extends ICode<CODE_ID>>>> extensions) {
      super(extensions);
    }

    public List<? extends CODE> execCreateCodes() {
      MethodInvocation<List<? extends CODE>> methodInvocation = new MethodInvocation<List<? extends CODE>>() {
        @SuppressWarnings("unchecked")
        @Override
        protected void callMethod(ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, ? extends ICode<CODE_ID>>> next) {
          setReturnValue((List<? extends CODE>) next.execCreateCodes(CodeTypeWithGenericCreateCodesChain.this));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }

  /**
   * chain for extending {@link AbstractCodeTypeWithGeneric#execCreateCode()}
   *
   * @param <CODE_TYPE_ID>
   * @param <CODE_ID>
   * @param <CODE>
   */
  public static class CodeTypeWithGenericCreateCodeChain<CODE_TYPE_ID, CODE_ID, CODE extends ICode<CODE_ID>> extends AbstractCodeTypeWithGenericChain<CODE_TYPE_ID, CODE_ID, CODE> {

    public CodeTypeWithGenericCreateCodeChain(List<? extends ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, ? extends ICode<CODE_ID>>>> extensions) {
      super(extensions);
    }

    public CODE execCreateCode(final ICodeRow<CODE_ID> newRow) {
      MethodInvocation<CODE> methodInvocation = new MethodInvocation<CODE>() {
        @SuppressWarnings("unchecked")
        @Override
        protected void callMethod(ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, ? extends ICode<CODE_ID>>> next) {
          setReturnValue((CODE) next.execCreateCode(CodeTypeWithGenericCreateCodeChain.this, newRow));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }

  /**
   * chain for extending {@link AbstractCodeTypeWithGeneric#execLoadCodes()}
   *
   * @param <CODE_TYPE_ID>
   * @param <CODE_ID>
   * @param <CODE>
   */
  public static class CodeTypeWithGenericLoadCodesChain<CODE_TYPE_ID, CODE_ID, CODE extends ICode<CODE_ID>> extends AbstractCodeTypeWithGenericChain<CODE_TYPE_ID, CODE_ID, CODE> {

    public CodeTypeWithGenericLoadCodesChain(List<? extends ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, ? extends ICode<CODE_ID>>>> extensions) {
      super(extensions);
    }

    public List<? extends ICodeRow<CODE_ID>> execLoadCodes(final Class<? extends ICodeRow<CODE_ID>> codeRowType) {
      MethodInvocation<List<? extends ICodeRow<CODE_ID>>> methodInvocation = new MethodInvocation<List<? extends ICodeRow<CODE_ID>>>() {
        @Override
        protected void callMethod(ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, ? extends ICode<CODE_ID>>> next) {
          setReturnValue(next.execLoadCodes(CodeTypeWithGenericLoadCodesChain.this, codeRowType));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }

  /**
   * chain for extending {@link AbstractCodeTypeWithGeneric#execOverwriteCode()}
   *
   * @param <CODE_TYPE_ID>
   * @param <CODE_ID>
   * @param <CODE>
   */
  public static class CodeTypeWithGenericOverwriteCodeChain<CODE_TYPE_ID, CODE_ID, CODE extends ICode<CODE_ID>> extends AbstractCodeTypeWithGenericChain<CODE_TYPE_ID, CODE_ID, CODE> {

    public CodeTypeWithGenericOverwriteCodeChain(List<? extends ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, ? extends ICode<CODE_ID>>>> extensions) {
      super(extensions);
    }

    public void execOverwriteCode(final ICodeRow<CODE_ID> oldCode, final ICodeRow<CODE_ID> newCode) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, ? extends ICode<CODE_ID>>> next) {
          next.execOverwriteCode(CodeTypeWithGenericOverwriteCodeChain.this, oldCode, newCode);
        }
      };
      callChain(methodInvocation);
    }
  }

}
