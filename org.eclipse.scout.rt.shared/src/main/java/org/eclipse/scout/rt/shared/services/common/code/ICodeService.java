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
package org.eclipse.scout.rt.shared.services.common.code;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.servicetunnel.RemoteServiceAccessDenied;

public interface ICodeService extends IService {

  <T extends ICodeType<?, ?>> T getCodeType(Class<T> type);

  /**
   * Searches for a code type given its id.
   * <p>
   * Note that this method does not load code types, but only searches code types already loaded into the code service
   * using {@link #getAllCodeTypes(String)}, {@link #getCodeType(Class)} etc.
   *
   * @param id
   * @return the type found or null
   */
  <T> ICodeType<T, ?> findCodeTypeById(T id);

  List<ICodeType<?, ?>> getCodeTypes(List<Class<? extends ICodeType<?, ?>>> types);

  Map<Class<? extends ICodeType<?, ?>>, ICodeType<?, ?>> getCodeTypeMap(Collection<Class<? extends ICodeType<?, ?>>> types);

  <CODE_ID_TYPE, CODE extends ICode<CODE_ID_TYPE>> CODE getCode(Class<CODE> type);

  /**
   * reload code type
   *
   * @return Non null unmodifiable list with reloaded code types.
   */
  <T extends ICodeType<?, ?>> T reloadCodeType(Class<T> type);

  /**
   * reload code types
   *
   * @return Non null unmodifiable list with reloaded code types.
   */
  List<ICodeType<?, ?>> reloadCodeTypes(List<Class<? extends ICodeType<?, ?>>> types);

  /**
   * @return all code type classes with classPrefix
   * @deprecated use {@link #getAllCodeTypeClasses()}. Will be removed in Scout 7.
   */
  @Deprecated
  Set<Class<? extends ICodeType<?, ?>>> getAllCodeTypeClasses(String classPrefix);

  /**
   * @return all code type classes
   */
  Set<Class<? extends ICodeType<?, ?>>> getAllCodeTypeClasses();

  /**
   * @deprecated use {@link #getAllCodeTypes()}. Will be removed in Scout 7.
   */
  @Deprecated
  @RemoteServiceAccessDenied
  Collection<ICodeType<?, ?>> getAllCodeTypes(String classPrefix);

  Collection<ICodeType<?, ?>> getAllCodeTypes();
}
