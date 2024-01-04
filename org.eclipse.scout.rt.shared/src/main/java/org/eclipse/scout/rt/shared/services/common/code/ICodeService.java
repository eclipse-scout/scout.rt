/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.code;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.service.IService;

public interface ICodeService extends IService {

  <T extends ICodeType<?, ?>> T getCodeType(Class<T> type);

  /**
   * Searches for a code type given its id.
   * <p>
   * Note that this method does not load code types, but only searches code types already loaded into the code service
   * using {@link #getCodeTypes(List)}, {@link #getCodeType(Class)} etc.
   *
   * @return the type found or null
   */
  <T> ICodeType<T, ?> findCodeTypeById(T id);

  List<ICodeType<?, ?>> getCodeTypes(List<Class<? extends ICodeType<?, ?>>> types);

  Map<Class<? extends ICodeType<?, ?>>, ICodeType<?, ?>> getCodeTypeMap(Collection<Class<? extends ICodeType<?, ?>>> types);

  <CODE extends ICode<?>> CODE getCode(Class<CODE> type);

  /**
   * reload code type
   *
   * @return Non-null unmodifiable list with reloaded code types.
   */
  <T extends ICodeType<?, ?>> T reloadCodeType(Class<T> type);

  /**
   * reload code types
   *
   * @return Non-null unmodifiable list with reloaded code types.
   */
  List<ICodeType<?, ?>> reloadCodeTypes(List<Class<? extends ICodeType<?, ?>>> types);

  /**
   * Invalidates the given code type without reloading it immediately.
   */
  <T extends ICodeType<?, ?>> void invalidateCodeType(Class<T> type);

  /**
   * Invalidates the given list of code types without reloading them immediately.
   */
  void invalidateCodeTypes(List<Class<? extends ICodeType<?, ?>>> types);

  /**
   * @return all code type classes
   */
  Set<Class<? extends ICodeType<?, ?>>> getAllCodeTypeClasses();

  Collection<ICodeType<?, ?>> getAllCodeTypes();
}
