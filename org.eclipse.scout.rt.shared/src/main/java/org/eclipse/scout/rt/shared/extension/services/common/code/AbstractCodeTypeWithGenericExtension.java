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

import org.eclipse.scout.rt.shared.extension.AbstractSerializableExtension;
import org.eclipse.scout.rt.shared.extension.services.common.code.CodeTypeWithGenericChains.CodeTypeWithGenericCreateCodeChain;
import org.eclipse.scout.rt.shared.extension.services.common.code.CodeTypeWithGenericChains.CodeTypeWithGenericCreateCodesChain;
import org.eclipse.scout.rt.shared.extension.services.common.code.CodeTypeWithGenericChains.CodeTypeWithGenericLoadCodesChain;
import org.eclipse.scout.rt.shared.extension.services.common.code.CodeTypeWithGenericChains.CodeTypeWithGenericOverwriteCodeChain;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeTypeWithGeneric;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.common.code.ICodeRow;

public abstract class AbstractCodeTypeWithGenericExtension<CODE_TYPE_ID, CODE_ID, CODE extends ICode<CODE_ID>, OWNER extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, CODE>> extends AbstractSerializableExtension<OWNER>
    implements ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, OWNER> {

  private static final long serialVersionUID = 1L;

  /**
   * @param owner
   */
  public AbstractCodeTypeWithGenericExtension(OWNER owner) {
    super(owner);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<? extends ICode<CODE_ID>> execCreateCodes(CodeTypeWithGenericCreateCodesChain chain) {
    return chain.execCreateCodes();
  }

  @SuppressWarnings("unchecked")
  @Override
  public ICode<CODE_ID> execCreateCode(CodeTypeWithGenericCreateCodeChain chain, ICodeRow<CODE_ID> newRow) {
    return chain.execCreateCode(newRow);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<? extends ICodeRow<CODE_ID>> execLoadCodes(CodeTypeWithGenericLoadCodesChain chain, Class<? extends ICodeRow<CODE_ID>> codeRowType) {
    return chain.execLoadCodes(codeRowType);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void execOverwriteCode(CodeTypeWithGenericOverwriteCodeChain chain, ICodeRow<CODE_ID> oldCode, ICodeRow<CODE_ID> newCode) {
    chain.execOverwriteCode(oldCode, newCode);
  }
}
