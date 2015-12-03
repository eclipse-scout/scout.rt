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

import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.services.common.code.CodeTypeWithGenericChains.CodeTypeWithGenericCreateCodeChain;
import org.eclipse.scout.rt.shared.extension.services.common.code.CodeTypeWithGenericChains.CodeTypeWithGenericCreateCodesChain;
import org.eclipse.scout.rt.shared.extension.services.common.code.CodeTypeWithGenericChains.CodeTypeWithGenericLoadCodesChain;
import org.eclipse.scout.rt.shared.extension.services.common.code.CodeTypeWithGenericChains.CodeTypeWithGenericOverwriteCodeChain;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.common.code.ICodeRow;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;

public interface ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, OWNER extends ICodeType<CODE_TYPE_ID, CODE_ID>> extends IExtension<OWNER> {

  List<? extends ICode<CODE_ID>> execCreateCodes(CodeTypeWithGenericCreateCodesChain chain);

  ICode<CODE_ID> execCreateCode(CodeTypeWithGenericCreateCodeChain chain, ICodeRow<CODE_ID> newRow);

  List<? extends ICodeRow<CODE_ID>> execLoadCodes(CodeTypeWithGenericLoadCodesChain chain, Class<? extends ICodeRow<CODE_ID>> codeRowType);

  void execOverwriteCode(CodeTypeWithGenericOverwriteCodeChain chain, ICodeRow<CODE_ID> oldCode, ICodeRow<CODE_ID> newCode);
}
