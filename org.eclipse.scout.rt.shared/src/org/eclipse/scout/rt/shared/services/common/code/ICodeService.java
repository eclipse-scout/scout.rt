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

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.osgi.BundleClassDescriptor;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelAccessDenied;
import org.eclipse.scout.service.IService;

@Priority(-3)
public interface ICodeService extends IService {

  <T extends ICodeType> T getCodeType(Class<T> type);

  <T extends ICodeType> T getCodeType(Long partitionId, Class<T> type);

  /**
   * @param id
   * @return the type found or null
   *         Note that this method does not load code types, but only searches code types already loaded into the code
   *         service using {@link #getAllCodeTypes(String)}, {@link #getCodeType(Class)} etc.
   */
  ICodeType findCodeTypeById(Object id);

  /**
   * @param partitionId
   * @param id
   * @return the type found or null
   *         Note that this method does not load code types, but only searches code types already loaded into the code
   *         service using {@link #getAllCodeTypes(String)}, {@link #getCodeType(Class)} etc.
   */
  ICodeType findCodeTypeById(Long partitionId, Object id);

  ICodeType[] getCodeTypes(Class... types);

  ICodeType[] getCodeTypes(Long partitionId, Class... types);

  <T extends ICode> T getCode(Class<T> type);

  <T extends ICode> T getCode(Long partitionId, Class<T> type);

  /**
   * reload code type
   */
  <T extends ICodeType> T reloadCodeType(Class<T> type);

  /**
   * reload code types
   */
  ICodeType[] reloadCodeTypes(Class... types);

  /**
   * @return all codetype classes from bundles with classPrefix
   */
  BundleClassDescriptor[] getAllCodeTypeClasses(String classPrefix);

  @ServiceTunnelAccessDenied
  ICodeType[] getAllCodeTypes(String classPrefix);

  @ServiceTunnelAccessDenied
  ICodeType[] getAllCodeTypes(String classPrefix, Long partitionId);
}
