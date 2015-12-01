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
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.shared.cache.ICache;

/**
 * Maintains a cache of ICodeType objects that can be (re)loaded using the methods reloadCodeType, reloadCodeTypes
 *
 * @since 4.3.0 (mars-M5)
 * @deprecated replaced with {@link ICache}. Will be removed in Scout 6.1.
 */
@Deprecated
public class CodeTypeCache {
  private Object m_cacheLock = new Object();
  private HashMap<Class<? extends ICodeType>, ICodeType> m_cache = new HashMap<Class<? extends ICodeType>, ICodeType>();

  public CodeTypeCache() {
  }

  @SuppressWarnings("unchecked")
  public <T> ICodeType<T, ?> findCodeTypeById(T id) {
    if (id == null) {
      return null;
    }
    synchronized (m_cacheLock) {
      for (ICodeType<?, ?> ct : m_cache.values()) {
        if (id.equals(ct.getId())) {
          return (ICodeType<T, ?>) ct;
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
          BEANS.get(ExceptionHandler.class).handle(new ProcessingException("error creating instance of class '" + type.getName() + "'.", t));
        }
      }
      return instance;
    }
  }

  public List<ICodeType<?, ?>> getCodeTypes(List<Class<? extends ICodeType<?, ?>>> types) {
    List<ICodeType<?, ?>> instances = new ArrayList<ICodeType<?, ?>>(types.size());
    for (Class<? extends ICodeType<?, ?>> codeTypeClazz : types) {
      instances.add(getCodeType(codeTypeClazz));
    }
    return instances;
  }

  @SuppressWarnings("unchecked")
  public <CODE_ID_TYPE, CODE extends ICode<CODE_ID_TYPE>> CODE getCode(final Class<CODE> type) {
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
        BEANS.get(ExceptionHandler.class).handle(new ProcessingException("error creating instance of class '" + type.getName() + "'.", t));
      }
    }
    ICodeType<?, CODE_ID_TYPE> codeType = getCodeType(declaringCodeTypeClass);
    final Holder<ICode<CODE_ID_TYPE>> codeHolder = new Holder<ICode<CODE_ID_TYPE>>();
    ICodeVisitor<ICode<CODE_ID_TYPE>> v = new ICodeVisitor<ICode<CODE_ID_TYPE>>() {
      @Override
      public boolean visit(ICode<CODE_ID_TYPE> code, int treeLevel) {
        if (code.getClass() == type) {
          codeHolder.setValue(code);
          return false;
        }
        return true;
      }
    };
    codeType.visit(v);
    return (CODE) codeHolder.getValue();
  }

  public <T extends ICodeType<?, ?>> T reloadCodeType(Class<T> type) {
    List<Class<? extends ICodeType<?, ?>>> codeTypeList = new ArrayList<Class<? extends ICodeType<?, ?>>>(1);
    codeTypeList.add(type);
    unloadCodeTypes(codeTypeList);
    return getCodeType(type);
  }

  public List<ICodeType<?, ?>> reloadCodeTypes(List<Class<? extends ICodeType<?, ?>>> types) {
    unloadCodeTypes(types);
    return getCodeTypes(types);
  }

  protected void unloadCodeTypes(List<Class<? extends ICodeType<?, ?>>> types) {
    synchronized (m_cacheLock) {
      for (Class<? extends ICodeType<?, ?>> codeTypeClazz : types) {
        m_cache.remove(codeTypeClazz);
      }
    }
  }
}
