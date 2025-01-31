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

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;

/**
 * This service creates a lookup call for a code type. It can be used to create custom {@link CodeLookupCall} instances.
 * <p>
 * Known callers are {@link ISmartField}, {@link IListBox}, {@link ITreeBox}, {@link IRadioButtonGroup},
 * {@link ISmartColumn}
 * <p>
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=388242
 *
 * @since 3.8.1
 */
public interface ICodeLookupCallFactoryService extends IService {

  <T> CodeLookupCall<T> newInstance(Class<? extends ICodeType<?, T>> codeTypeClass);
}
