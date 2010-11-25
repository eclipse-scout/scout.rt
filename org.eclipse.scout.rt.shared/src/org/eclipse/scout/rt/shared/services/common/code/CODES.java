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

import org.eclipse.scout.service.SERVICES;

/**
 * Convenience accessor for service ICodeService
 */
public final class CODES {

  private CODES() {
  }

  public static <T extends ICodeType> T getCodeType(Class<T> type) {
    return SERVICES.getService(ICodeService.class).getCodeType(type);
  }

  /**
   * @param id
   * @return
   *         Note that this method does not load code types, but only searches code types already loaded into the code
   *         service using {@link #getAllCodeTypes(String)}, {@link #getCodeType(Class)} etc.
   */
  public static ICodeType findCodeTypeById(Object id) {
    return SERVICES.getService(ICodeService.class).findCodeTypeById(id);
  }

  /**
   * @param id
   * @return
   *         Note that this method does not load code types, but only searches code types already loaded into the code
   *         service using {@link #getAllCodeTypes(String)}, {@link #getCodeType(Class)} etc.
   */
  public static ICodeType findCodeTypeById(Long partitionId, Object id) {
    return SERVICES.getService(ICodeService.class).findCodeTypeById(partitionId, id);
  }

  public static ICodeType[] getCodeTypes(Class... types) {
    return SERVICES.getService(ICodeService.class).getCodeTypes(types);
  }

  public static <T extends ICode> T getCode(Class<T> type) {
    return SERVICES.getService(ICodeService.class).getCode(type);
  }

  public static <T extends ICodeType> T reloadCodeType(Class<T> type) {
    return SERVICES.getService(ICodeService.class).reloadCodeType(type);
  }

  public static ICodeType[] reloadCodeTypes(Class... types) {
    return SERVICES.getService(ICodeService.class).reloadCodeTypes(types);
  }

  public static ICodeType[] getAllCodeTypes(String classPrefix) {
    return SERVICES.getService(ICodeService.class).getAllCodeTypes(classPrefix);
  }
}
