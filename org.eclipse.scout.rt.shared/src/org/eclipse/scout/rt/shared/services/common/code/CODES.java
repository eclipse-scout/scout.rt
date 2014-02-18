/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.code;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.service.SERVICES;

/**
 * Convenience accessor for service ICodeService
 */
public final class CODES {

  private CODES() {
  }

  public static <T extends ICodeType<?, ?>> T getCodeType(Class<T> type) {
    return SERVICES.getService(ICodeService.class).getCodeType(type);
  }

  /**
   * @param id
   * @return
   *         Note that this method does not load code types, but only searches code types already loaded into the code
   *         service using {@link #getAllCodeTypes(String)}, {@link #getCodeType(Class)} etc.
   */
  public static <T> ICodeType<T, ?> findCodeTypeById(T id) {
    return SERVICES.getService(ICodeService.class).findCodeTypeById(id);
  }

  /**
   * @param id
   * @return
   *         Note that this method does not load code types, but only searches code types already loaded into the code
   *         service using {@link #getAllCodeTypes(String)}, {@link #getCodeType(Class)} etc.
   */
  public static <T> ICodeType<T, ?> findCodeTypeById(Long partitionId, T codeTypeId) {
    return SERVICES.getService(ICodeService.class).findCodeTypeById(partitionId, codeTypeId);
  }

  public static List<ICodeType<?, ?>> getCodeTypes(Class<? extends ICodeType<?, ?>>... types) {
    List<Class<? extends ICodeType<?, ?>>> typeList = new ArrayList<Class<? extends ICodeType<?, ?>>>();
    if (types != null) {
      for (Class<? extends ICodeType<?, ?>> t : types) {
        typeList.add(t);
      }
    }
    return SERVICES.getService(ICodeService.class).getCodeTypes(typeList);
  }

  public static List<ICodeType<?, ?>> getCodeTypes(List<Class<? extends ICodeType<?, ?>>> types) {
    return SERVICES.getService(ICodeService.class).getCodeTypes(types);
  }

  public static <CODE_ID_TYPE, CODE extends ICode<CODE_ID_TYPE>> CODE getCode(Class<CODE> type) {
    return SERVICES.getService(ICodeService.class).getCode(type);
  }

  public static <T extends ICodeType> T reloadCodeType(Class<T> type) throws ProcessingException {
    return SERVICES.getService(ICodeService.class).reloadCodeType(type);
  }

  public static List<ICodeType<?, ?>> reloadCodeTypes(Class<? extends ICodeType<?, ?>>... types) throws ProcessingException {
    List<Class<? extends ICodeType<?, ?>>> typeList = new ArrayList<Class<? extends ICodeType<?, ?>>>();
    if (types != null) {
      for (Class<? extends ICodeType<?, ?>> t : types) {
        typeList.add(t);
      }
    }
    return SERVICES.getService(ICodeService.class).reloadCodeTypes(typeList);
  }

  public static List<ICodeType<?, ?>> reloadCodeTypes(List<Class<? extends ICodeType<?, ?>>> types) throws ProcessingException {
    return SERVICES.getService(ICodeService.class).reloadCodeTypes(types);
  }

  public static List<ICodeType<?, ?>> getAllCodeTypes(String classPrefix) {
    return SERVICES.getService(ICodeService.class).getAllCodeTypes(classPrefix);
  }
}
