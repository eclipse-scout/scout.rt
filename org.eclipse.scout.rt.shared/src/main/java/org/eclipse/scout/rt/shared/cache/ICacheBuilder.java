/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.cache;

import org.eclipse.scout.commons.BeanUtility;
import org.eclipse.scout.rt.platform.Bean;

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
   * @param clusterEnabled
   *          if true a cache that runs in a clustered server (<code>IClusterSynchronizationService#isEnabled()</code>)
   *          will publish cache invalidations to the cluster. (Default true)
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
