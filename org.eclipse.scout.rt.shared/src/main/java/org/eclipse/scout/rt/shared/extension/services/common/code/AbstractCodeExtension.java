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

import org.eclipse.scout.rt.shared.extension.AbstractSerializableExtension;
import org.eclipse.scout.rt.shared.extension.services.common.code.CodeChains.CodeCreateChildCodesChain;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCode;
import org.eclipse.scout.rt.shared.services.common.code.ICode;

public abstract class AbstractCodeExtension<T, OWNER extends AbstractCode<T>> extends AbstractSerializableExtension<OWNER> implements ICodeExtension<T, OWNER> {
  private static final long serialVersionUID = 1L;

  /**
   * @param owner
   */
  public AbstractCodeExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public List<? extends ICode<T>> execCreateChildCodes(CodeCreateChildCodesChain<T> chain) {
    return chain.execCreateChildCodes();
  }

}
