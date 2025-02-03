/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
