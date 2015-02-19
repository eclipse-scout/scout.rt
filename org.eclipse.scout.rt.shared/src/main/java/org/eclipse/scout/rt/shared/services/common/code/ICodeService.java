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

import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.osgi.BundleClassDescriptor;
import org.eclipse.scout.rt.shared.servicetunnel.RemoteServiceAccessDenied;
import org.eclipse.scout.rt.shared.validate.IValidationStrategy;
import org.eclipse.scout.rt.shared.validate.InputValidation;
import org.eclipse.scout.service.IService;

@Priority(-3)
@InputValidation(IValidationStrategy.PROCESS.class)
public interface ICodeService extends IService {

  <T extends ICodeType<?, ?>> T getCodeType(Class<T> type);

  <T extends ICodeType<?, ?>> T getCodeType(Long partitionId, Class<T> type);

  /**
   * @param id
   * @return the type found or null
   *         Note that this method does not load code types, but only searches code types already loaded into the code
   *         service using {@link #getAllCodeTypes(String)}, {@link #getCodeType(Class)} etc.
   */
  <T> ICodeType<T, ?> findCodeTypeById(T id);

  /**
   * @param partitionId
   * @param id
   * @return the type found or null
   *         Note that this method does not load code types, but only searches code types already loaded into the code
   *         service using {@link #getAllCodeTypes(String)}, {@link #getCodeType(Class)} etc.
   */
  <T> ICodeType<T, ?> findCodeTypeById(Long partitionId, T id);

  List<ICodeType<?, ?>> getCodeTypes(List<Class<? extends ICodeType<?, ?>>> types);

  List<ICodeType<?, ?>> getCodeTypes(Long partitionId, List<Class<? extends ICodeType<?, ?>>> types);

  <CODE_ID_TYPE, CODE extends ICode<CODE_ID_TYPE>> CODE getCode(Class<CODE> type);

  <CODE_ID_TYPE, CODE extends ICode<CODE_ID_TYPE>> CODE getCode(Long partitionId, Class<CODE> type);

  /**
   * reload code type
   * 
   * @throws ProcessingException
   */
  <T extends ICodeType<?, ?>> T reloadCodeType(Class<T> type) throws ProcessingException;

  /**
   * reload code types
   * 
   * @throws ProcessingException
   */
  List<ICodeType<?, ?>> reloadCodeTypes(List<Class<? extends ICodeType<?, ?>>> types) throws ProcessingException;

  /**
   * @return all codetype classes from bundles with classPrefix
   */
  Set<BundleClassDescriptor> getAllCodeTypeClasses(String classPrefix);

  @RemoteServiceAccessDenied
  List<ICodeType<?, ?>> getAllCodeTypes(String classPrefix);

  @RemoteServiceAccessDenied
  List<ICodeType<?, ?>> getAllCodeTypes(String classPrefix, Long partitionId);
}
