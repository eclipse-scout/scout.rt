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
package org.eclipse.scout.rt.shared.cache;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.BeanUtility;

/**
 * All caches should be build through this builder. After building a cache, <b>do not</b> surround it with another
 * cache-wrapper. Instead use {@link #withAdditionalCustomWrapper(Class, Object...)} to add additional behavior.
 *
 * @param <K>
 *          the type of keys maintained by this cache
 * @param <V>
 *          the type of mapped values
 * @since 5.2
 */
@Bean
public interface ICacheBuilder<K, V> {

  /**
   * Calls {@link ICacheBuilderService} which creates a new cache according to this builder
   *
   * @throws IllegalStateException
   *           if no cacheId or value-resolver is set
   * @throws IllegalArgumentException
   *           if an additional custom wrapper does not have a public constructor with a single argument of type
   *           {@link ICache} or cannot be instantiated
   * @return created cache instance
   */
  ICache<K, V> build();

  /**
   * Required property which must be for each cache unique.
   *
   * @param cacheId
   *          for the created cache
   * @return this builder
   */
  ICacheBuilder<K, V> withCacheId(String cacheId);

  /**
   * Required property which is used to resolve a value of a key.
   *
   * @param valueResolver
   * @return this builder
   * @see ICacheValueResolver
   */
  ICacheBuilder<K, V> withValueResolver(ICacheValueResolver<K, V> valueResolver);

  /**
   * @param shared
   *          a shared cache is used in client and server part of the application. Clients are notified about cache
   *          invalidations at server but not vice versa. (Default false)
   * @return this builder
   */
  ICacheBuilder<K, V> withShared(boolean shared);

  /**
   * @param threadSafe
   *          if set to false, cache does not use a thread-safe implementation. (Default true)
   * @return this builder
   */
  ICacheBuilder<K, V> withThreadSafe(boolean threadSafe);

  /**
   * @param atomicInsertion
   *          if set to true, a cache might concurrently resolve a key, but the get operations that issued these
   *          resolves will return the same value. This option is only valid if threadSafe is set to true. Typically set
   *          this option to true if the cache value is modifiable. (Default false)
   * @return this builder
   */
  ICacheBuilder<K, V> withAtomicInsertion(boolean atomicInsertion);

  /**
   * @param clusterEnabled
   *          if true a cache that runs in a clustered server (<code>IClusterSynchronizationService#isEnabled()</code>)
   *          will publish cache invalidations to the cluster. (Default false)
   * @return this builder
   */
  ICacheBuilder<K, V> withClusterEnabled(boolean clusterEnabled);

  /**
   * @param transactional
   *          if true changes in a server cache will be published to other transactions at commit phase. (Default false)
   * @return this builder
   */
  ICacheBuilder<K, V> withTransactional(boolean transactional);

  /**
   * @param transactionalFastForward
   *          if true a fresh resolved value for which no previous mapping exists in a transactional server cache is
   *          directly publish to other transactions rather than at commit phase. (Default false)
   * @return this builder
   */
  ICacheBuilder<K, V> withTransactionalFastForward(boolean transactionalFastForward);

  /**
   * @param singleton
   *          if true the created cache is used to cache at most one item. The cache builder can return a more efficient
   *          implementation. The cache implementation is not required to enforce one value. (Default false)
   * @return this builder
   */
  ICacheBuilder<K, V> withSingleton(boolean singleton);

  /**
   * If booth arguments are not null, any resolved cache value will expire and have to be resolved again after the given
   * time to live duration.
   * <p>
   * <b>Note:</b> If one uses additionally the option {@link #withSizeBound(Integer)} the parameter <tt>touchOnGet</tt>
   * is overruled and set to true.
   * <p>
   * Read accesses through {@link ICache#getUnmodifiableMap()} do <em>not</em> reset the time to live duration of a
   * cache entry.
   *
   * @param timeToLiveDuration
   *          time to live duration
   * @param timeToLiveUnit
   *          time to live unit
   * @param touchOnGet
   *          if true getting an value will reset the time to live of a cache entry
   * @return this builder
   * @throws IllegalArgumentException
   *           if readTimeToLive is negative
   */
  ICacheBuilder<K, V> withTimeToLive(Long timeToLiveDuration, TimeUnit timeToLiveUnit, boolean touchOnGet);

  /**
   * If set to a non null value, the maximum number of cached values is bounded. The provided size bound is <em>not</em>
   * enforced and is just a guidance value.
   * <p>
   * The current policy that is used to evict elements is least recently used (LRU).
   *
   * @param sizeBound
   *          the target size that map should have approximately.
   * @return this builder
   */
  ICacheBuilder<K, V> withSizeBound(Integer sizeBound);

  /**
   * <b>Warning: Potential deadlock</b>
   * <p>
   * Can be used to bound the maximum number of concurrent value resolve operations (non-fair). There is a risk of
   * deadlocks if the {@link #valueResolver(ICacheValueResolver)} itself may be blocked.
   *
   * @param maxConcurrentResolve
   *          maximum concurrent resolves
   * @return this builder
   * @throws IllegalArgumentException
   *           if maxConcurrentResolve is negative
   */
  ICacheBuilder<K, V> withMaxConcurrentResolve(Integer maxConcurrentResolve);

  /**
   * Adds an additional cache wrapper to the constructed cache instance. In the cache instance these additional wrappers
   * are ordered in the same sequence as they were added. The cache wrapper is created always through a constructor that
   * takes as first argument an {@link ICache}. See also {@link BeanUtility#createInstance(Class, Object...)} for
   * creation details.
   *
   * @param cacheClass
   * @param arguments
   *          any additional arguments (beside the first {@link ICache} argument) used to create the cache wrapper
   *          instance
   * @return this builder
   */
  ICacheBuilder<K, V> withAdditionalCustomWrapper(Class<? extends ICache> cacheClass, Object... arguments);

  /**
   * Removes the additional custom cache wrapper of the given type.
   *
   * @param cacheClass
   */
  void removeAdditionalCustomWrapper(Class<? extends ICache> cacheClass);

}
