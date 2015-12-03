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
