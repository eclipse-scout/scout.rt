/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.lookup;

import org.eclipse.scout.rt.shared.services.common.code.ICodeType;

/**
 * @since 3.8.1
 */
public class DefaultCodeLookupCallFactoryService implements ICodeLookupCallFactoryService {

  @Override
  public <T> CodeLookupCall<T> newInstance(Class<? extends ICodeType<?, T>> codeTypeClass) {
    return new CodeLookupCall<>(codeTypeClass);
  }

}
