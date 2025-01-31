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

import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.services.common.code.CodeChains.CodeCreateChildCodesChain;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCode;
import org.eclipse.scout.rt.shared.services.common.code.ICode;

public interface ICodeExtension<T, OWNER extends AbstractCode<T>> extends IExtension<OWNER> {

  List<? extends ICode<T>> execCreateChildCodes(CodeCreateChildCodesChain<T> chain);
}
