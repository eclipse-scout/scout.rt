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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.osgi.BundleClassDescriptor;
import org.eclipse.scout.commons.runtime.BundleBrowser;
import org.eclipse.scout.rt.client.Activator;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.clientnotification.ClientNotificationConsumerEvent;
import org.eclipse.scout.rt.client.services.common.clientnotification.IClientNotificationConsumerListener;
import org.eclipse.scout.rt.client.services.common.clientnotification.IClientNotificationConsumerService;
import org.eclipse.scout.rt.client.servicetunnel.ServiceTunnelUtility;
import org.eclipse.scout.rt.shared.services.common.code.CodeTypeChangedNotification;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.common.code.ICodeService;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.common.code.ICodeVisitor;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.SERVICES;
import org.osgi.framework.Bundle;

/**
 * maintains a cache of ICodeType objects that can be (re)loaded using the
 * methods loadCodeType, loadCodeTypes if getters and finders are called with
 * partitionId, cache is not used.
 * <p>
 * Service state is per [{@link IClientSession}.class,{@link Locale#getDefault()},partitionId]
 */
@Priority(-3)
public class CodeServiceClientProxy extends AbstractService implements ICodeService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(CodeServiceClientProxy.class);

  private final Object m_stateLock = new Object();
  private final HashMap<CompositeObject, ServiceState> m_stateMap = new HashMap<CompositeObject, ServiceState>();

  public CodeServiceClientProxy() {
  }

  private ServiceState getServiceState() {
    return getServiceState(null);
  }

  private ServiceState getServiceState(Long partitionId) {
    IClientSession session = ClientJob.getCurrentSession();
    if (session == null) {
      LOG.warn("could not find a client session");
      return null;
    }
    if (partitionId == null) {
      if (session.getSharedVariableMap().containsKey(ICodeType.PROP_PARTITION_ID)) {
        partitionId = (Long) session.getSharedVariableMap().get(ICodeType.PROP_PARTITION_ID);
      }
    }
    CompositeObject key = new CompositeObject(session.getClass(), Locale.getDefault(), partitionId);
    synchronized (m_stateLock) {
      ServiceState data = (ServiceState) m_stateMap.get(key);
      if (data == null) {
        data = new ServiceState();
        m_stateMap.put(key, data);
      }
      return data;
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  public void initializeService() {
    super.initializeService();
    // add client notification listener
    SERVICES.getService(IClientNotificationConsumerService.class).addGlobalClientNotificationConsumerListener(new IClientNotificationConsumerListener() {
      @Override
      public void handleEvent(final ClientNotificationConsumerEvent e, boolean sync) {
        if (e.getClientNotification().getClass() == CodeTypeChangedNotification.class) {
          if (sync) {
            try {
              reloadCodeTypes(((CodeTypeChangedNotification) e.getClientNotification()).getCodeTypes());
            }
            catch (Throwable t) {
              LOG.error("update due to client notification", t);
              // nop
            }
          }
          else {
            new ClientSyncJob("Reload code types", ClientSyncJob.getCurrentSession()) {
              @Override
              protected void runVoid(IProgressMonitor monitor) throws Throwable {
                reloadCodeTypes(((CodeTypeChangedNotification) e.getClientNotification()).getCodeTypes());
              }
            }.schedule();
          }
        }
      }
    });
  }

  @Override
  public <T extends ICodeType> T getCodeType(Class<T> type) {
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
  public <T extends ICodeType> T getCodeType(Long partitionId, Class<T> type) {
    T instance = getRemoteService().getCodeType(partitionId, type);
    return instance;
  }

  @Override
  public ICodeType findCodeTypeById(Object id) {
    if (id == null) {
      return null;
    }
    ServiceState state = getServiceState();
    synchronized (state.m_cacheLock) {
      for (ICodeType ct : state.m_cache.values()) {
        if (id.equals(ct.getId())) {
          return ct;
        }
      }
    }
    return null;
  }

  @Override
  public ICodeType findCodeTypeById(Long partitionId, Object id) {
    if (id == null) {
      return null;
    }
    ServiceState state = getServiceState(partitionId);
    synchronized (state.m_cacheLock) {
      for (ICodeType ct : state.m_cache.values()) {
        if (id.equals(ct.getId())) {
          return ct;
        }
      }
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public ICodeType[] getCodeTypes(Class... types) {
    ArrayList<Class> missingTypes = new ArrayList<Class>();
    ICodeType[] instances = new ICodeType[types.length];
    ServiceState state = getServiceState();
    synchronized (state.m_cacheLock) {
      for (int i = 0; i < types.length; i++) {
        instances[i] = state.m_cache.get(types[i]);
        if (instances[i] == null) {
          missingTypes.add(types[i]);
        }
      }
    }
    if (missingTypes.size() > 0) {
      ICodeType[] newInstances = getRemoteService().getCodeTypes(missingTypes.toArray(new Class[0]));
      synchronized (state.m_cacheLock) {
        int k = 0;
        for (int i = 0; i < types.length; i++) {
          if (instances[i] == null) {
            instances[i] = newInstances[k];
            if (instances[i] != null) {
              state.m_cache.put(types[i], instances[i]);
            }
            k++;
          }
        }
      }
    }
    return instances;
  }

  @Override
  public ICodeType[] getCodeTypes(Long partitionId, Class... types) {
    ICodeType[] codeTypes = getRemoteService().getCodeTypes(partitionId, types);
    return codeTypes;
  }

  private <T extends ICode> Class getDeclaringCodeTypeClass(final Class<T> type) {
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
      catch (Throwable t) {
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
  @SuppressWarnings("unchecked")
  public <T extends ICode> T getCode(final Class<T> type) {
    Class declaringCodeTypeClass = getDeclaringCodeTypeClass(type);
    ICodeType codeType = getCodeType(declaringCodeTypeClass);
    return findCode(type, codeType);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends ICode> T getCode(Long partitionId, final Class<T> type) {
    Class declaringCodeTypeClass = getDeclaringCodeTypeClass(type);
    ICodeType codeType = getCodeType(partitionId, declaringCodeTypeClass);
    return findCode(type, codeType);
  }

  @Override
  public <T extends ICodeType> T reloadCodeType(Class<T> type) {
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
  @SuppressWarnings("unchecked")
  public ICodeType[] reloadCodeTypes(Class... types) {
    for (int i = 0; i < types.length; i++) {
      unloadCodeType(types[i]);
    }
    // do NOT call reload on the backend service, clients can not reload codes,
    // they can just refresh their local cache
    // In order to reload a code, the call to reload has to be placed on the
    // server
    ICodeType[] instances = getRemoteService().getCodeTypes(types);
    ServiceState state = getServiceState();
    synchronized (state.m_cacheLock) {
      for (int i = 0; i < types.length; i++) {
        if (instances[i] != null) {
          state.m_cache.put(types[i], instances[i]);
        }
      }
    }
    return instances;
  }

  @Override
  public BundleClassDescriptor[] getAllCodeTypeClasses(String classPrefix) {
    if (classPrefix == null) {
      return new BundleClassDescriptor[0];
    }
    ServiceState state = getServiceState();
    synchronized (state.m_codeTypeClassDescriptorMapLock) {
      BundleClassDescriptor[] a = state.m_codeTypeClassDescriptorMap.get(classPrefix);
      if (a != null) {
        return a;
      }
      //
      HashSet<BundleClassDescriptor> localCodeTypes = new HashSet<BundleClassDescriptor>();
      for (Bundle bundle : Activator.getDefault().getBundle().getBundleContext().getBundles()) {
        if (bundle.getSymbolicName().startsWith(classPrefix)) {
          // ok
        }
        else if (classPrefix.startsWith(bundle.getSymbolicName() + ".")) {
          // ok
        }
        else {
          continue;
        }
        String[] classNames;
        try {
          BundleBrowser bundleBrowser = new BundleBrowser(bundle.getSymbolicName(), "");
          classNames = bundleBrowser.getClasses(false, true);
        }
        catch (Exception e1) {
          LOG.warn(null, e1);
          continue;
        }
        // filter
        for (String className : classNames) {
          // fast pre-check
          if (className.indexOf("CodeType") >= 0) {
            try {
              Class c = null;
              c = bundle.loadClass(className);
              if (ICodeType.class.isAssignableFrom(c)) {
                if (!c.isInterface()) {
                  int flags = c.getModifiers();
                  if (Modifier.isPublic(flags) && (!Modifier.isAbstract(flags)) && (!c.getSimpleName().startsWith("Abstract"))) {
                    if (ICodeType.class.isAssignableFrom(c)) {
                      localCodeTypes.add(new BundleClassDescriptor(bundle.getSymbolicName(), c.getName()));
                    }
                  }
                }
              }
            }
            catch (Throwable t) {
              // nop
            }
          }
        }
      }
      // check with server side list
      HashSet<BundleClassDescriptor> mergeSet = new HashSet<BundleClassDescriptor>();
      BundleClassDescriptor[] remoteCodeTypes = getRemoteService().getAllCodeTypeClasses(classPrefix);
      for (BundleClassDescriptor d : remoteCodeTypes) {
        if (localCodeTypes.remove(d)) {
          mergeSet.add(d);
        }
        else {
          LOG.error("Missing code-type in client: " + d.getClassName() + ", defined in server-side bundle " + d.getBundleSymbolicName());
        }
      }
      for (BundleClassDescriptor d : localCodeTypes) {
        LOG.error("Phantom code-type in client: " + d.getClassName() + ", defined in client-side bundle " + d.getBundleSymbolicName());
      }
      //
      a = mergeSet.toArray(new BundleClassDescriptor[mergeSet.size()]);
      state.m_codeTypeClassDescriptorMap.put(classPrefix, a);
      return a;
    }
  }

  @Override
  public ICodeType[] getAllCodeTypes(String classPrefix) {
    ArrayList<Class> list = new ArrayList<Class>();
    for (BundleClassDescriptor d : getAllCodeTypeClasses(classPrefix)) {
      try {
        list.add(Platform.getBundle(d.getBundleSymbolicName()).loadClass(d.getClassName()));
      }
      catch (Throwable t) {
        LOG.warn("Loading " + d.getClassName() + " of bundle " + d.getBundleSymbolicName(), t);
        continue;
      }
    }
    return getCodeTypes(list.toArray(new Class[list.size()]));
  }

  @Override
  public ICodeType[] getAllCodeTypes(String classPrefix, Long partitionId) {
    return getAllCodeTypes(classPrefix);
  }

  private <T extends ICodeType> void unloadCodeType(Class<T> type) {
    ServiceState state = getServiceState();
    synchronized (state.m_cacheLock) {
      state.m_cache.remove(type);
    }
  }

  private ICodeService getRemoteService() {
    return ServiceTunnelUtility.createProxy(ICodeService.class, ClientSyncJob.getCurrentSession().getServiceTunnel());
  }

  private static class ServiceState {
    final Object m_cacheLock = new Object();
    final HashMap<Class<? extends ICodeType>, ICodeType> m_cache = new HashMap<Class<? extends ICodeType>, ICodeType>();
    //
    final Object m_codeTypeClassDescriptorMapLock = new Object();
    final HashMap<String, BundleClassDescriptor[]> m_codeTypeClassDescriptorMap = new HashMap<String, BundleClassDescriptor[]>();
  }
}
