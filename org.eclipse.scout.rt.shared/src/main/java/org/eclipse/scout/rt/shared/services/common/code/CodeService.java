/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformExceptionTranslator;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.shared.cache.AbstractCacheValueResolver;
import org.eclipse.scout.rt.shared.cache.ICache;
import org.eclipse.scout.rt.shared.cache.ICacheBuilder;
import org.eclipse.scout.rt.shared.cache.ICacheValueResolver;
import org.eclipse.scout.rt.shared.servicetunnel.RemoteServiceAccessDenied;

/**
 * Common logic for the {@link ICodeService} implementations. Uses {@link ICache} for caching.
 *
 * @since 4.3.0 (Mars-M5)
 */
@Order(5100)
@CreateImmediately
public class CodeService implements ICodeService {

  public static final String CODE_SERVICE_CACHE_ID = CodeService.class.getName();

  private volatile ICache<CodeTypeCacheKey, ICodeType<?, ?>> m_cache;

  @PostConstruct
  protected void initCache() {
    m_cache = createCacheBuilder().build();
  }

  /**
   * Can be overridden to customize the cache builder
   *
   * @return {@link ICacheBuilder} for the internal cache
   */
  protected ICacheBuilder<CodeTypeCacheKey, ICodeType<?, ?>> createCacheBuilder() {
    @SuppressWarnings("unchecked")
    ICacheBuilder<CodeTypeCacheKey, ICodeType<?, ?>> cacheBuilder = BEANS.get(ICacheBuilder.class);
    return cacheBuilder.withCacheId(CODE_SERVICE_CACHE_ID).withValueResolver(createCacheValueResolver()).withShared(true).withClusterEnabled(true).withTransactional(true).withTransactionalFastForward(true);
  }

  protected ICacheValueResolver<CodeTypeCacheKey, ICodeType<?, ?>> createCacheValueResolver() {
    return new AbstractCacheValueResolver<CodeTypeCacheKey, ICodeType<?, ?>>() {

      @Override
      public ICodeType<?, ?> resolve(CodeTypeCacheKey key) {
        try {
          return key.getCodeTypeClass().newInstance();
        }
        catch (ReflectiveOperationException e) {
          throw BEANS.get(PlatformExceptionTranslator.class)
              .translate(e)
              .withContextInfo("key", key)
              .withContextInfo("codeTypeClass", key.getCodeTypeClass());
        }
      }
    };
  }

  protected ICache<CodeTypeCacheKey, ICodeType<?, ?>> getCache() {
    return m_cache;
  }

  /**
   * Creates a new cache key. Method hook allows to customize cache key instances.
   *
   * @param type
   * @return new cache key
   */
  protected <T extends ICodeType<?, ?>> CodeTypeCacheKey createCacheKey(Class<T> type) {
    if (type == null) {
      return null;
    }
    return new CodeTypeCacheKey(type);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends ICodeType<?, ?>> T getCodeType(Class<T> type) {
    return (T) getCache().get(createCacheKey(type));
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
  protected <T> ICodeType<T, ?> findCodeTypeByIdInternal(T id) {
    Locale locale = NlsLocale.get();
    for (Entry<CodeTypeCacheKey, ICodeType<?, ?>> entry : getCache().getUnmodifiableMap().entrySet()) {
      CodeTypeCacheKey key = entry.getKey();
      if (CompareUtility.equals(key.getLocale(), locale)) {
        ICodeType<?, ?> ct = entry.getValue();
        if (ct != null && id.equals(ct.getId())) {
          return (ICodeType<T, ?>) ct;
        }
      }
    }
    return null;
  }

  @Override
  public List<ICodeType<?, ?>> getCodeTypes(List<Class<? extends ICodeType<?, ?>>> types) {
    Map<Class<? extends ICodeType<?, ?>>, ICodeType<?, ?>> codeTypeMap = getCodeTypeMap(types);
    List<ICodeType<?, ?>> result = new ArrayList<>();
    for (Class<? extends ICodeType<?, ?>> type : types) {
      result.add(codeTypeMap.get(type));
    }
    return result;
  }

  @Override
  public Map<Class<? extends ICodeType<?, ?>>, ICodeType<?, ?>> getCodeTypeMap(Collection<Class<? extends ICodeType<?, ?>>> types) {
    // we perform a batch lookup in the cache in order to minimize calls to the cache
    List<CodeTypeCacheKey> keys = new ArrayList<>();
    for (Class<? extends ICodeType<?, ?>> type : types) {
      keys.add(createCacheKey(type));
    }
    Map<CodeTypeCacheKey, ICodeType<?, ?>> valueMap = getCache().getAll(keys);
    Map<Class<? extends ICodeType<?, ?>>, ICodeType<?, ?>> result = new HashMap<>();
    for (Entry<CodeTypeCacheKey, ICodeType<?, ?>> entry : valueMap.entrySet()) {
      result.put(entry.getKey().getCodeTypeClass(), entry.getValue());
    }
    return result;
  }

  @Override
  public <CODE_ID_TYPE, CODE extends ICode<CODE_ID_TYPE>> CODE getCode(Class<CODE> type) {
    Class<? extends ICodeType<?, CODE_ID_TYPE>> declaringCodeTypeClass = getDeclaringCodeTypeClass(type);
    ICodeType<?, CODE_ID_TYPE> codeType = getCodeType(declaringCodeTypeClass);
    return findCode(type, codeType);
  }

  @SuppressWarnings("unchecked")
  protected <T> Class<? extends ICodeType<?, T>> getDeclaringCodeTypeClass(final Class<? extends ICode<T>> type) {
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
      catch (Exception e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }
    return declaringCodeTypeClass;
  }

  @SuppressWarnings("unchecked")
  protected <T> T findCode(final Class<T> type, ICodeType codeType) {
    if (codeType == null) {
      return null;
    }
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
  public <T extends ICodeType<?, ?>> T reloadCodeType(Class<T> type) {
    if (type == null) {
      return null;
    }
    getCache().invalidate(new CodeTypeCacheEntryFilter(type), true);
    return getCodeType(type);
  }

  @Override
  public List<ICodeType<?, ?>> reloadCodeTypes(List<Class<? extends ICodeType<?, ?>>> types) {
    CodeTypeCacheEntryFilter filter = new CodeTypeCacheEntryFilter(types);
    if (filter.getCodeTypeClasses().isEmpty()) {
      return Collections.emptyList();
    }
    getCache().invalidate(filter, true);
    return getCodeTypes(types);
  }

  @SuppressWarnings("deprecation")
  @Deprecated
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

  @Override
  public Set<Class<? extends ICodeType<?, ?>>> getAllCodeTypeClasses() {
    return BEANS.get(CodeTypeClassInventory.class).getClasses();
  }

  @Override
  public List<ICodeType<?, ?>> getAllCodeTypes() {
    Set<Class<? extends ICodeType<?, ?>>> allCodeTypeClasses = getAllCodeTypeClasses();
    List<Class<? extends ICodeType<?, ?>>> list = CollectionUtility.arrayList(allCodeTypeClasses);
    return getCodeTypes(list);
  }

  @SuppressWarnings("deprecation")
  @Override
  @RemoteServiceAccessDenied
  @Deprecated
  public List<ICodeType<?, ?>> getAllCodeTypes(String classPrefix) {
    Set<Class<? extends ICodeType<?, ?>>> allCodeTypeClasses = getAllCodeTypeClasses(classPrefix);
    List<Class<? extends ICodeType<?, ?>>> list = CollectionUtility.arrayList(allCodeTypeClasses);
    return getCodeTypes(list);
  }
}
