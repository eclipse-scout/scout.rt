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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.shared.servicetunnel.RemoteServiceAccessDenied;
import org.eclipse.scout.service.AbstractService;

/**
 * Common logic for the {@link ICodeService} implementations.
 * Delegates to {@link CodeTypeStore}
 *
 * @since 4.3.0 (Mars-M5)
 */
public class SharedCodeService extends AbstractService implements ICodeService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SharedCodeService.class);
  private static final Long DEFAULT_PARTITION_ID = 0L;

  private final CodeTypeStore m_codeTypeStore;
  private final Object m_codeTypeClassDescriptorMapLock;
  private final Map<String, Set<Class<? extends ICodeType<?, ?>>>> m_codeTypeClassDescriptorMap;

  public SharedCodeService() {
    m_codeTypeStore = new CodeTypeStore();
    m_codeTypeClassDescriptorMapLock = new Object();
    m_codeTypeClassDescriptorMap = new HashMap<>();
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
    if (classPrefix == null) {
      return CollectionUtility.hashSet();
    }
    synchronized (m_codeTypeClassDescriptorMapLock) {
      Set<Class<? extends ICodeType<?, ?>>> a = m_codeTypeClassDescriptorMap.get(classPrefix);
      if (a != null) {
        return CollectionUtility.hashSet(a);
      }

      Set<IClassInfo> allKnownCodeTypes = Platform.get().getClassInventory().getAllKnownSubClasses(ICodeType.class);
      Set<Class<? extends ICodeType<?, ?>>> discoveredCodeTypes = new HashSet<>(allKnownCodeTypes.size());
      for (IClassInfo codeTypeInfo : allKnownCodeTypes) {
        if (acceptClassName(codeTypeInfo.name())) {
          try {
            if (acceptClass(codeTypeInfo)) {
              @SuppressWarnings("unchecked")
              Class<? extends ICodeType<?, ?>> codeTypeClass = (Class<? extends ICodeType<?, ?>>) codeTypeInfo.resolveClass();
              discoveredCodeTypes.add(codeTypeClass);
            }
          }
          catch (ClassNotFoundException e) {
            LOG.error("Unable to load code type.", e);
          }
        }
      }
      m_codeTypeClassDescriptorMap.put(classPrefix, CollectionUtility.hashSet(discoveredCodeTypes));
      return CollectionUtility.hashSet(discoveredCodeTypes);
    }
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
   * Checks whether the given class name is a potential code type class. Class names that do not meet the
   * requirements of this method are not considered further, i.e. the "expensive" class instantiation is skipped.
   * The default implementation checks whether the class name contains <code>"CodeType"</code>.
   *
   * @param bundle
   *          The class's hosting bundle
   * @param className
   *          the class name to be checked
   * @return Returns <code>true</code> if the given class name meets the requirements to be considered as a code type
   *         class. <code>false</code> otherwise.
   */
  protected boolean acceptClassName(String className) {
    return (className.indexOf("CodeType") >= 0);
  }

  /**
   * Checks whether the given class is a CodeType class that should be visible to this service.
   *
   * @param c
   *          the class to be checked
   * @return Returns <code>true</code> if the class is an accepted code type class. <code>false</code> otherwise.
   */
  protected boolean acceptClass(IClassInfo c) {
    return c.isInstanciable();
  }

  /**
   * Method to provide the current partitionId.
   * Child classes can override this method to get the partitionId from the session.
   *
   * @return partitionId
   */
  protected Long provideCurrentPartitionId() {
    return DEFAULT_PARTITION_ID;
  }
}
