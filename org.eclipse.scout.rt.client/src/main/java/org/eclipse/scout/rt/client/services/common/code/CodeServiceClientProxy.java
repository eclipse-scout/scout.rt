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
package org.eclipse.scout.rt.client.services.common.code;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.Client;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.platform.service.AbstractService;
import org.eclipse.scout.rt.shared.notification.INotificationHandler;
import org.eclipse.scout.rt.shared.services.common.code.CodeTypeChangedNotification;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.common.code.ICodeService;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.common.code.ICodeVisitor;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelUtility;

/**
 * maintains a cache of ICodeType objects that can be (re)loaded using the
 * methods loadCodeType, loadCodeTypes if getters and finders are called with
 * partitionId, cache is not used.
 * <p>
 * Service state is per [{@link IClientSession}.class,{@link NlsLocale#get()},partitionId]
 */
@Client
@Order(3)
@CreateImmediately
@ApplicationScoped
public class CodeServiceClientProxy extends AbstractService implements ICodeService, INotificationHandler<CodeTypeChangedNotification> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(CodeServiceClientProxy.class);

  private final Object m_stateLock = new Object();
  private final Map<CompositeObject, ServiceState> m_stateMap = new HashMap<>();

  public CodeServiceClientProxy() {
  }

  private ServiceState getServiceState() {
    return getServiceState(null);
  }

  private ServiceState getServiceState(Long partitionId) {
    IClientSession session = ClientSessionProvider.currentSession();
    if (session == null) {
      LOG.warn("could not find a client session");
      return null;
    }
    if (partitionId == null) {
      if (session.getSharedVariableMap().containsKey(ICodeType.PROP_PARTITION_ID)) {
        partitionId = (Long) session.getSharedVariableMap().get(ICodeType.PROP_PARTITION_ID);
      }
    }
    CompositeObject key = new CompositeObject(session.getClass(), NlsLocale.get(), partitionId);
    synchronized (m_stateLock) {
      ServiceState data = (ServiceState) m_stateMap.get(key);
      if (data == null) {
        data = new ServiceState();
        m_stateMap.put(key, data);
      }
      return data;
    }
  }

  @Override
  public void handleNotification(CodeTypeChangedNotification notification) {
    try {
      reloadCodeTypes(new ArrayList<Class<? extends ICodeType<?, ?>>>(notification.getCodeTypes()));
    }
    catch (Exception t) {
      LOG.error("update due to client notification", t);
    }
  }

  @Override
  public <T extends ICodeType<?, ?>> T getCodeType(Class<T> type) {
    ServiceState state = getServiceState();
    synchronized (state.m_cacheLock) {
      @SuppressWarnings("unchecked")
      T instance = (T) state.m_cache.get(type);
      if (instance == null) {
        instance = getRemoteService().getCodeType(type);
        if (instance != null) {
          state.m_cache.put(type, instance);
        }
      }
      return instance;
    }
  }

  @Override
  public <T extends ICodeType<?, ?>> T getCodeType(Long partitionId, Class<T> type) {
    T instance = getRemoteService().getCodeType(partitionId, type);
    return instance;
  }

  @Override
  public <T> ICodeType<T, ?> findCodeTypeById(T id) {
    if (id == null) {
      return null;
    }
    ICodeType<T, ?> ct = findCodeTypeByIdInternal(id);
    if (ct != null) {
      return ct;
    }
    // populate code type cache
    getAllCodeTypes("");
    return findCodeTypeByIdInternal(id);
  }

  /**
   * @return Returns the code type with the given id or <code>null</code> if it is not found in the cache.
   */
  @SuppressWarnings("unchecked")
  private <T> ICodeType<T, ?> findCodeTypeByIdInternal(T id) {
    ServiceState state = getServiceState();
    synchronized (state.m_cacheLock) {
      for (ICodeType<?, ?> ct : state.m_cache.values()) {
        if (id.equals(ct.getId())) {
          return (ICodeType<T, ?>) ct;
        }
      }
    }
    return null;
  }

  @Override
  public <T> ICodeType<T, ?> findCodeTypeById(Long partitionId, T id) {
    if (id == null) {
      return null;
    }
    ICodeType<T, ?> ct = findCodeTypeByIdInternal(partitionId, id);
    if (ct != null) {
      return ct;
    }
    // populate code type cache
    getAllCodeTypes("");
    return findCodeTypeByIdInternal(partitionId, id);
  }

  /**
   * @return Returns the code type with the given id and partition or <code>null</code> if it is not found in the cache.
   */
  @SuppressWarnings("unchecked")
  private <T> ICodeType<T, ?> findCodeTypeByIdInternal(Long partitionId, T id) {
    ServiceState state = getServiceState(partitionId);
    synchronized (state.m_cacheLock) {
      for (ICodeType<?, ?> ct : state.m_cache.values()) {
        if (id.equals(ct.getId())) {
          return (ICodeType<T, ?>) ct;
        }
      }
    }
    return null;
  }

  @Override
  public List<ICodeType<?, ?>> getCodeTypes(List<Class<? extends ICodeType<?, ?>>> types) {
    List<Class<? extends ICodeType<?, ?>>> missingTypes = new ArrayList<Class<? extends ICodeType<?, ?>>>();
    List<ICodeType<?, ?>> instances = new ArrayList<ICodeType<?, ?>>(types.size());
    ServiceState state = getServiceState();
    synchronized (state.m_cacheLock) {
      for (Class<? extends ICodeType<?, ?>> codeTypeClazz : types) {
        ICodeType<?, ?> codeType = state.m_cache.get(codeTypeClazz);
        if (codeType != null) {
          instances.add(codeType);
        }
        else {
          missingTypes.add(codeTypeClazz);
        }
      }
      if (missingTypes.size() > 0) {
        List<ICodeType<?, ?>> newInstances = getRemoteService().getCodeTypes(missingTypes);
        Iterator<ICodeType<?, ?>> iNewInstances = newInstances.iterator();
        for (Class<? extends ICodeType<?, ?>> type : missingTypes) {
          if (iNewInstances.hasNext()) {
            ICodeType<?, ?> instance = iNewInstances.next();
            if (instance != null) {
              state.m_cache.put(type, instance);
              instances.add(instance);
            }
          }
        }
      }
    }
    return instances;
  }

  @Override
  public List<ICodeType<?, ?>> getCodeTypes(Long partitionId, List<Class<? extends ICodeType<?, ?>>> types) {
    return getRemoteService().getCodeTypes(partitionId, types);
  }

  @SuppressWarnings("unchecked")
  private <T> Class<? extends ICodeType<?, T>> getDeclaringCodeTypeClass(final Class<? extends ICode<T>> type) {
    if (type == null) {
      return null;
    }
    Class declaringCodeTypeClass = null;
    if (type.getDeclaringClass() != null) {
      // code is inner type of code type or another code
      Class c = type.getDeclaringClass();
      while (c != null && !(ICodeType.class.isAssignableFrom(c))) {
        c = c.getDeclaringClass();
      }
      declaringCodeTypeClass = c;
    }
    if (declaringCodeTypeClass == null) {
      try {
        declaringCodeTypeClass = type.newInstance().getCodeType().getClass();
      }
      catch (Exception t) {
        LOG.error("find code " + type, t);
      }
    }
    return declaringCodeTypeClass;
  }

  @SuppressWarnings("unchecked")
  private <T> T findCode(final Class<T> type, ICodeType codeType) {
    final Holder<ICode> codeHolder = new Holder<ICode>(ICode.class);
    ICodeVisitor v = new ICodeVisitor() {
      @Override
      public boolean visit(ICode code, int treeLevel) {
        if (code.getClass() == type) {
          codeHolder.setValue(code);
          return false;
        }
        return true;
      }
    };
    codeType.visit(v);
    return (T) codeHolder.getValue();
  }

  @Override
  public <CODE_ID_TYPE, CODE extends ICode<CODE_ID_TYPE>> CODE getCode(Class<CODE> type) {
    Class<? extends ICodeType<?, CODE_ID_TYPE>> declaringCodeTypeClass = getDeclaringCodeTypeClass(type);
    ICodeType<?, CODE_ID_TYPE> codeType = getCodeType(declaringCodeTypeClass);
    return findCode(type, codeType);
  }

  @Override
  public <CODE_ID_TYPE, CODE extends ICode<CODE_ID_TYPE>> CODE getCode(Long partitionId, Class<CODE> type) {
    Class<? extends ICodeType<?, CODE_ID_TYPE>> declaringCodeTypeClass = getDeclaringCodeTypeClass(type);
    ICodeType<?, CODE_ID_TYPE> codeType = getCodeType(partitionId, declaringCodeTypeClass);
    return findCode(type, codeType);
  }

  @Override
  public <T extends ICodeType<?, ?>> T reloadCodeType(Class<T> type) throws ProcessingException {
    unloadCodeType(type);
    // do NOT call reload on the backend service, clients can not reload codes,
    // they can just refresh their local cache
    // In order to reload a code, the call to reload has to be placed on the
    // server
    T instance = getRemoteService().getCodeType(type);
    ServiceState state = getServiceState();
    synchronized (state.m_cacheLock) {
      if (instance != null) {
        state.m_cache.put(type, instance);
      }
    }
    return instance;
  }

  @Override
  public List<ICodeType<?, ?>> reloadCodeTypes(List<Class<? extends ICodeType<?, ?>>> types) throws ProcessingException {
    for (Class<? extends ICodeType<?, ?>> codeTypeClazz : types) {
      unloadCodeType(codeTypeClazz);
    }
    // do NOT call reload on the backend service, clients can not reload codes,
    // they can just refresh their local cache
    // In order to reload a code, the call to reload has to be placed on the
    // server
    List<ICodeType<?, ?>> instances = getRemoteService().getCodeTypes(types);
    ServiceState state = getServiceState();
    synchronized (state.m_cacheLock) {
      int i = 0;
      for (Class<? extends ICodeType<?, ?>> codeTypeClazz : types) {
        ICodeType<?, ?> codeInstance = CollectionUtility.getElement(instances, i);
        if (codeInstance != null) {
          state.m_cache.put(codeTypeClazz, codeInstance);
        }
        i++;
      }
    }
    return instances;
  }

  @Override
  public Set<Class<? extends ICodeType<?, ?>>> getAllCodeTypeClasses(String classPrefix) {
    if (classPrefix == null) {
      return CollectionUtility.hashSet();
    }
    ServiceState state = getServiceState();
    synchronized (state.m_codeTypeClassDescriptorMapLock) {
      Set<Class<? extends ICodeType<?, ?>>> a = state.m_codeTypeClassDescriptorMap.get(classPrefix);
      if (a != null) {
        return CollectionUtility.hashSet(a);
      }
      // load code types from server-side
      Set<Class<? extends ICodeType<?, ?>>> verifiedCodeTypes = new HashSet<>();
      Collection<Class<? extends ICodeType<?, ?>>> remoteCodeTypes = getRemoteService().getAllCodeTypeClasses(classPrefix);
      ClassLoader classLoader = getClass().getClassLoader();
      for (Class<? extends ICodeType<?, ?>> d : remoteCodeTypes) {
        try {
          // check whether code type is available on client-side
          classLoader.loadClass(d.getName());
          verifiedCodeTypes.add(d);
        }
        catch (Exception t) {
          LOG.error("Missing code-type in client: " + d.getName());
        }
      }

      state.m_codeTypeClassDescriptorMap.put(classPrefix, verifiedCodeTypes);
      return verifiedCodeTypes;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<ICodeType<?, ?>> getAllCodeTypes(String classPrefix) {
    List<Class<? extends ICodeType<?, ?>>> list = new ArrayList<Class<? extends ICodeType<?, ?>>>();
    ClassLoader classLoader = getClass().getClassLoader();
    for (Class<? extends ICodeType<?, ?>> d : getAllCodeTypeClasses(classPrefix)) {
      try {
        list.add((Class<? extends ICodeType<?, ?>>) classLoader.loadClass(d.getName()));
      }
      catch (Exception t) {
        LOG.warn("Loading " + d.getName(), t);
        continue;
      }
    }
    return getCodeTypes(list);
  }

  @Override
  public List<ICodeType<?, ?>> getAllCodeTypes(String classPrefix, Long partitionId) {
    return getAllCodeTypes(classPrefix);
  }

  private <T extends ICodeType> void unloadCodeType(Class<T> type) {
    ServiceState state = getServiceState();
    synchronized (state.m_cacheLock) {
      state.m_cache.remove(type);
    }
  }

  protected ICodeService getRemoteService() {
    return ServiceTunnelUtility.createProxy(ICodeService.class);
  }

  private static class ServiceState {
    final Object m_cacheLock = new Object();
    final Map<Class<? extends ICodeType<?, ?>>, ICodeType<?, ?>> m_cache = new HashMap<Class<? extends ICodeType<?, ?>>, ICodeType<?, ?>>();
    //
    final Object m_codeTypeClassDescriptorMapLock = new Object();
    final Map<String, Set<Class<? extends ICodeType<?, ?>>>> m_codeTypeClassDescriptorMap = new HashMap<>();
  }
}
