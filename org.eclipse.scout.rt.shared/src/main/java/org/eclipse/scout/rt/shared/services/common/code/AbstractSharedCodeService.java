/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.service.AbstractService;
import org.eclipse.scout.rt.shared.servicetunnel.RemoteServiceAccessDenied;

/**
 * Common logic for the {@link ICodeService} implementations. Delegates to {@link CodeTypeStore}
 *
 * @since 4.3.0 (Mars-M5)
 */
public abstract class AbstractSharedCodeService extends AbstractService implements ICodeService {
  private static final Long DEFAULT_PARTITION_ID = 0L;

  private final CodeTypeStore m_codeTypeStore;

  protected AbstractSharedCodeService() {
    m_codeTypeStore = new CodeTypeStore();
  }

  @Override
  public <T extends ICodeType<?, ?>> T getCodeType(Class<T> type) {
    return getCodeTypeCache().getCodeType(type);
  }

  @Override
  public <T extends ICodeType<?, ?>> T getCodeType(Long partitionId, Class<T> type) {
    return getCodeTypeCache(partitionId).getCodeType(type);
  }

  @Override
  public <T> ICodeType<T, ?> findCodeTypeById(T id) {
    if (id == null) {
      return null;
    }
    ICodeType<T, ?> ct = getCodeTypeCache().findCodeTypeById(id);
    if (ct != null) {
      return ct;
    }
    // populate code type cache
    getAllCodeTypes("");
    return getCodeTypeCache().findCodeTypeById(id);
  }

  @Override
  public <T> ICodeType<T, ?> findCodeTypeById(Long partitionId, T id) {
    if (id == null) {
      return null;
    }
    ICodeType<T, ?> ct = getCodeTypeCache(partitionId).findCodeTypeById(id);
    if (ct != null) {
      return ct;
    }
    // populate code type cache
    getAllCodeTypes("");
    return getCodeTypeCache(partitionId).findCodeTypeById(id);
  }

  @Override
  public List<ICodeType<?, ?>> getCodeTypes(List<Class<? extends ICodeType<?, ?>>> types) {
    return getCodeTypeCache().getCodeTypes(types);
  }

  @Override
  public List<ICodeType<?, ?>> getCodeTypes(Long partitionId, List<Class<? extends ICodeType<?, ?>>> types) {
    return getCodeTypeCache(partitionId).getCodeTypes(types);
  }

  @Override
  public <CODE_ID_TYPE, CODE extends ICode<CODE_ID_TYPE>> CODE getCode(Class<CODE> type) {
    return getCodeTypeCache().getCode(type);
  }

  @Override
  public <CODE_ID_TYPE, CODE extends ICode<CODE_ID_TYPE>> CODE getCode(final Long partitionId, final Class<CODE> type) {
    return getCodeTypeCache(partitionId).getCode(type);
  }

  @Override
  public <T extends ICodeType<?, ?>> T reloadCodeType(Class<T> type) throws ProcessingException {
    if (type == null) {
      return null;
    }
    List<Class<? extends ICodeType<?, ?>>> codetypeList = new ArrayList<Class<? extends ICodeType<?, ?>>>();
    codetypeList.add(type);

    m_codeTypeStore.unloadCodeTypeCache(codetypeList);

    notifyReloadCodeTypes(codetypeList);
    return getCodeTypeCache().reloadCodeType(type);
  }

  @Override
  public List<ICodeType<?, ?>> reloadCodeTypes(List<Class<? extends ICodeType<?, ?>>> types) throws ProcessingException {
    if (types == null) {
      return null;
    }
    m_codeTypeStore.unloadCodeTypeCache(types);

    notifyReloadCodeTypes(types);
    return getCodeTypeCache().reloadCodeTypes(types);
  }

  /**
   * Hook to notify clients and clusters during {@link #reloadCodeType(Class)} and {@link #reloadCodeTypes(List)}
   *
   * @since 4.3.0 (Mars-M5)
   * @param codetypeList
   * @throws ProcessingException
   */
  protected void notifyReloadCodeTypes(List<Class<? extends ICodeType<?, ?>>> codetypeList) throws ProcessingException {
  }

  protected List<ICodeType<?, ?>> reloadCodeTypesNoFire(List<Class<? extends ICodeType<?, ?>>> types) throws ProcessingException {
    if (types == null) {
      return null;
    }
    m_codeTypeStore.unloadCodeTypeCacheNoFire(types);
    return reloadCodeTypes(types);
  }

  @Override
  public Set<Class<? extends ICodeType<?, ?>>> getAllCodeTypeClasses(String classPrefix) {
    final Set<Class<? extends ICodeType<?, ?>>> filteredClasses = new LinkedHashSet<>();
    final Collection<Class<? extends ICodeType<?, ?>>> classes = getAllCodeTypeClasses();
    for (Class<? extends ICodeType<?, ?>> c : classes) {
      if (c.getName().startsWith(classPrefix)) {
        filteredClasses.add(c);
      }
    }
    return filteredClasses;
  }

  public Set<Class<? extends ICodeType<?, ?>>> getAllCodeTypeClasses() {
    return BEANS.get(CodeTypeClassInventory.class).getClasses();
  }

  @Override
  public List<ICodeType<?, ?>> getAllCodeTypes(String classPrefix) {
    Set<Class<? extends ICodeType<?, ?>>> allCodeTypeClasses = getAllCodeTypeClasses(classPrefix);
    List<Class<? extends ICodeType<?, ?>>> list = CollectionUtility.arrayList(allCodeTypeClasses);
    return getCodeTypes(list);
  }

  @Override
  @RemoteServiceAccessDenied
  public List<ICodeType<?, ?>> getAllCodeTypes(String classPrefix, Long partitionId) {
    return getAllCodeTypes(classPrefix);
  }

  private CodeTypeCache getCodeTypeCache() {
    return getCodeTypeCache(provideCurrentPartitionId());
  }

  private CodeTypeCache getCodeTypeCache(Long partitionId) {
    return m_codeTypeStore.getCodeTypeCache(partitionId, NlsLocale.get());
  }

  /**
   * Method to provide the current partitionId. Child classes can override this method to get the partitionId from the
   * session.
   *
   * @return partitionId
   */
  protected Long provideCurrentPartitionId() {
    return DEFAULT_PARTITION_ID;
  }
}
