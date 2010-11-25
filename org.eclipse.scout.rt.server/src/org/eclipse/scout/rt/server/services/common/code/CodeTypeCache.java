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
package org.eclipse.scout.rt.server.services.common.code;

import java.util.HashMap;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.common.code.ICodeVisitor;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

/**
 * Maintains a cache of ICodeType objects that can be (re)loaded using the
 * methods reloadCodeType, reloadCodeTypes
 */
public class CodeTypeCache {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(CodeTypeCache.class);

  private Object m_cacheLock = new Object();
  private HashMap<Class<? extends ICodeType>, ICodeType> m_cache = new HashMap<Class<? extends ICodeType>, ICodeType>();

  public CodeTypeCache() {
  }

  public ICodeType findCodeTypeById(Object id) {
    if (id == null) return null;
    synchronized (m_cacheLock) {
      for (ICodeType ct : m_cache.values()) {
        if (id.equals(ct.getId())) {
          return ct;
        }
      }
    }
    return null;
  }

  public <T extends ICodeType> T getCodeType(Class<T> type) {
    synchronized (m_cacheLock) {
      @SuppressWarnings("unchecked")
      T instance = (T) m_cache.get(type);
      if (instance == null) {
        try {
          instance = type.newInstance();
          m_cache.put(type, instance);
        }
        catch (Throwable t) {
          SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("create " + type.getName(), t));
        }
      }
      return instance;
    }
  }

  @SuppressWarnings("unchecked")
  public ICodeType[] getCodeTypes(Class... types) {
    ICodeType[] instances = new ICodeType[types.length];
    for (int i = 0; i < instances.length; i++) {
      instances[i] = getCodeType(types[i]);
    }
    return instances;
  }

  @SuppressWarnings("unchecked")
  public <T extends ICode> T getCode(final Class<T> type) {
    if (type == null) return null;
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
    ICodeType codeType = getCodeType(declaringCodeTypeClass);
    final Holder<ICode> codeHolder = new Holder<ICode>(ICode.class);
    ICodeVisitor v = new ICodeVisitor() {
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

  public <T extends ICodeType> T reloadCodeType(Class<T> type) {
    unloadCodeTypes(new Class[]{type});
    return getCodeType(type);
  }

  public ICodeType[] reloadCodeTypes(Class... types) {
    unloadCodeTypes(types);
    return getCodeTypes(types);
  }

  protected void unloadCodeTypes(Class[] types) {
    synchronized (m_cacheLock) {
      for (int i = 0; i < types.length; i++) {
        m_cache.remove(types[i]);
      }
    }
  }
}
