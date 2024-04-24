/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.BeanUtility;
import org.eclipse.scout.rt.platform.util.NumberUtility;
import org.eclipse.scout.rt.platform.util.collection.ConcurrentExpiringMap;
import org.eclipse.scout.rt.platform.util.collection.ConcurrentTransactionalMap;
import org.eclipse.scout.rt.platform.util.collection.CopyOnWriteTransactionalMap;

/**
 * @since 5.2
 */
@Order(5100)
public class CacheBuilder<K, V> implements ICacheBuilder<K, V> {

  private final List<CustomWrapperInitializer> m_customWrappers;

  private String m_cacheId;
  private Supplier<String> m_labelSupplier;
  private ICacheValueResolver<K, V> m_valueResolver;
  private boolean m_shared;
  private boolean m_remoteValueResolverEnabled;
  private boolean m_threadSafe;
  private boolean m_clusterEnabled;
  private boolean m_transactional;
  private boolean m_transactionalFastForward;
  private boolean m_singleton;
  private Long m_timeToLive;
  private boolean m_touchOnGet;
  private Integer m_sizeBound;
  private Integer m_maxConcurrentResolve;
  private boolean m_throwIfExists;
  private boolean m_replaceIfExists;

  public CacheBuilder() {
    m_customWrappers = new ArrayList<>();
    m_remoteValueResolverEnabled = true;
    m_threadSafe = true;
    m_throwIfExists = true;
    m_replaceIfExists = false;
  }

  @Override
  public ICache<K, V> build() {
    if (getCacheId() == null) {
      throw new IllegalStateException("cacheId is null");
    }
    Map<K, V> cacheMap = createCacheMap();
    ICache<K, V> cache = createBasicCache(cacheMap);
    cache = addBeforeCustomWrappers(cache);
    cache = addCustomWrappers(cache);
    cache = addAfterCustomWrappers(cache);
    if (isReplaceIfExists()) {
      registerAndReplace(cache);
      return cache;
    }
    if (isThrowIfExists()) {
      register(cache);
      return cache;
    }
    return registerIfAbsent(cache);
  }

  protected ICache<K, V> registerIfAbsent(ICache<K, V> cache) {
    return BEANS.get(ICacheRegistryService.class).registerIfAbsent(cache);
  }

  protected void registerAndReplace(ICache<K, V> cache) {
    BEANS.get(ICacheRegistryService.class).registerAndReplace(cache);
  }

  protected void register(ICache<K, V> cache) {
    BEANS.get(ICacheRegistryService.class).register(cache);
  }

  protected Map<K, V> createCacheMap() {
    if (!isCreateExpiringMap() && isTransactional() && (isSingleton() || !isTransactionalFastForward())) {
      return new CopyOnWriteTransactionalMap<>(getCacheId(), isTransactionalFastForward());
    }
    else if (isCreateExpiringMap()) {
      boolean touchOnGet = isTouchOnGet() || getSizeBound() != null;
      long timeToLive = NumberUtility.nvl(getTimeToLive(), -1L);
      int targetSize = NumberUtility.nvl(getSizeBound(), -1);
      return new ConcurrentExpiringMap<>(createConcurrentMap(), timeToLive, touchOnGet, targetSize);
    }
    else if (isThreadSafe() || isTransactional()) {
      return createConcurrentMap();
    }
    return new HashMap<>();
  }

  protected <KK, VV> ConcurrentMap<KK, VV> createConcurrentMap() {
    if (isTransactional()) {
      return new ConcurrentTransactionalMap<>(getCacheId(), isTransactionalFastForward());
    }
    else {
      return new ConcurrentHashMap<>();
    }
  }

  protected boolean isCreateExpiringMap() {
    return getTimeToLive() != null || getSizeBound() != null;
  }

  protected ICache<K, V> createBasicCache(Map<K, V> cacheMap) {
    return new BasicCache<>(getCacheId(), getLabelSupplier(), getValueResolver(), cacheMap);
  }

  protected ICache<K, V> addBeforeCustomWrappers(ICache<K, V> cache) {
    if (getMaxConcurrentResolve() != null) {
      cache = new BoundedResolveCacheWrapper<>(cache, getMaxConcurrentResolve());
    }
    return cache;
  }

  @SuppressWarnings("unchecked")
  protected ICache<K, V> addCustomWrappers(ICache<K, V> cache) {
    for (CustomWrapperInitializer initializer : getCustomWrappers()) {
      try {
        // take the original argument array and append at first position
        // as arguments the current cache instance
        Object[] baseArguments = initializer.getArguments();
        Object[] arguments = new Object[baseArguments.length + 1];
        System.arraycopy(baseArguments, 0, arguments, 1, baseArguments.length);
        arguments[0] = cache;
        // create the new instance
        cache = BeanUtility.createInstance(initializer.getWrapperClass(), arguments);
      }
      catch (Exception e) {
        throw new IllegalArgumentException(String.format("Failed creating cache instance of %s", initializer), e);
      }
    }
    return cache;
  }

  protected ICache<K, V> addAfterCustomWrappers(ICache<K, V> cache) {
    return cache;
  }

  @Override
  public CacheBuilder<K, V> withCacheId(String cacheId) {
    m_cacheId = cacheId;
    return this;
  }

  public String getCacheId() {
    return m_cacheId;
  }

  @Override
  public ICacheBuilder<K, V> withLabelSupplier(Supplier<String> labelSupplier) {
    m_labelSupplier = labelSupplier;
    return this;
  }

  public Supplier<String> getLabelSupplier() {
    return m_labelSupplier;
  }

  @Override
  public ICacheBuilder<K, V> withReplaceIfExists(boolean b) {
    m_replaceIfExists = b;
    return this;
  }

  public boolean isReplaceIfExists() {
    return m_replaceIfExists;
  }

  @Override
  public ICacheBuilder<K, V> withThrowIfExists(boolean b) {
    m_throwIfExists = b;
    return this;
  }

  public boolean isThrowIfExists() {
    return m_throwIfExists;
  }

  @Override
  public CacheBuilder<K, V> withValueResolver(ICacheValueResolver<K, V> valueResolver) {
    m_valueResolver = valueResolver;
    return this;
  }

  public ICacheValueResolver<K, V> getValueResolver() {
    return m_valueResolver;
  }

  @Override
  public CacheBuilder<K, V> withShared(boolean shared) {
    m_shared = shared;
    return this;
  }

  public boolean isShared() {
    return m_shared;
  }

  @Override
  public CacheBuilder<K, V> withRemoteValueResolverEnabled(boolean remoteValueResolverEnabled) {
    m_remoteValueResolverEnabled = remoteValueResolverEnabled;
    return this;
  }

  public boolean isRemoteValueResolverEnabled() {
    return m_remoteValueResolverEnabled;
  }

  @Override
  public CacheBuilder<K, V> withThreadSafe(boolean threadSafe) {
    m_threadSafe = threadSafe;
    return this;
  }

  public boolean isThreadSafe() {
    return m_threadSafe;
  }

  @Override
  public CacheBuilder<K, V> withClusterEnabled(boolean clusterEnabled) {
    m_clusterEnabled = clusterEnabled;
    return this;
  }

  public boolean isClusterEnabled() {
    return m_clusterEnabled;
  }

  @Override
  public CacheBuilder<K, V> withTransactional(boolean transactional) {
    m_transactional = transactional;
    return this;
  }

  public boolean isTransactional() {
    return m_transactional;
  }

  @Override
  public CacheBuilder<K, V> withTransactionalFastForward(boolean transactionalFastForward) {
    m_transactionalFastForward = transactionalFastForward;
    return this;
  }

  public boolean isTransactionalFastForward() {
    return m_transactionalFastForward;
  }

  @Override
  public CacheBuilder<K, V> withSingleton(boolean singleton) {
    m_singleton = singleton;
    return this;
  }

  public boolean isSingleton() {
    return m_singleton;
  }

  @Override
  public CacheBuilder<K, V> withTimeToLive(Long timeToLiveDuration, TimeUnit timeToLiveUnit, boolean touchOnGet) {
    if (timeToLiveDuration == null || timeToLiveUnit == null) {
      m_timeToLive = null;
    }
    else {
      if (timeToLiveDuration < 0L) {
        throw new IllegalArgumentException("timeToLiveDuration cannot be negative");
      }
      m_timeToLive = timeToLiveUnit.toMillis(timeToLiveDuration);
      m_touchOnGet = touchOnGet;
    }
    return this;
  }

  /**
   * @return time to live duration in milliseconds
   */
  public Long getTimeToLive() {
    return m_timeToLive;
  }

  public boolean isTouchOnGet() {
    return m_touchOnGet;
  }

  @Override
  public CacheBuilder<K, V> withSizeBound(Integer sizeBound) {
    m_sizeBound = sizeBound;
    return this;
  }

  public Integer getSizeBound() {
    return m_sizeBound;
  }

  @Override
  public CacheBuilder<K, V> withMaxConcurrentResolve(Integer maxConcurrentResolve) {
    if (maxConcurrentResolve != null && maxConcurrentResolve < 0) {
      throw new IllegalArgumentException("maxConcurrentResolve cannot be negative");
    }
    m_maxConcurrentResolve = maxConcurrentResolve;
    return this;
  }

  public Integer getMaxConcurrentResolve() {
    return m_maxConcurrentResolve;
  }

  @Override
  public CacheBuilder<K, V> withAdditionalCustomWrapper(Class<? extends ICache> cacheClass, Object... arguments) {
    if (cacheClass != null) {
      m_customWrappers.add(new CustomWrapperInitializer(cacheClass, arguments));
    }
    return this;
  }

  @Override
  public void removeAdditionalCustomWrapper(Class<? extends ICache> cacheClass) {
    if (cacheClass == null) {
      return;
    }
    m_customWrappers.removeIf(initializer -> cacheClass.equals(initializer.getWrapperClass()));
  }

  /**
   * @return life list of custom wrappers; useful for subclasses
   */
  protected List<CustomWrapperInitializer> getCustomWrappers() {
    return m_customWrappers;
  }

  @Override
  public String toString() {
    return "CacheBuilder [m_cacheId=" + m_cacheId + "]";
  }

  protected static class CustomWrapperInitializer {
    private final Class<? extends ICache> m_wrapperClass;
    private final Object[] m_arguments;

    public CustomWrapperInitializer(Class<? extends ICache> wrapperClass, Object[] arguments) {
      super();
      m_wrapperClass = wrapperClass;
      m_arguments = arguments;
    }

    public Class<? extends ICache> getWrapperClass() {
      return m_wrapperClass;
    }

    public Object[] getArguments() {
      return m_arguments;
    }

    @Override
    public String toString() {
      return "CustomWrapperInitializer [m_wrapperClass=" + m_wrapperClass + ", m_arguments=" + Arrays.toString(m_arguments) + "]";
    }
  }
}
