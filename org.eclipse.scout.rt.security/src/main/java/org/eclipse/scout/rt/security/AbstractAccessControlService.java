/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.security;

import java.security.AccessController;
import java.security.PermissionCollection;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.annotation.PostConstruct;
import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.cache.AllCacheEntryFilter;
import org.eclipse.scout.rt.platform.cache.ICache;
import org.eclipse.scout.rt.platform.cache.ICacheBuilder;
import org.eclipse.scout.rt.platform.cache.ICacheValueResolver;
import org.eclipse.scout.rt.platform.cache.KeyCacheEntryFilter;
import org.eclipse.scout.rt.platform.context.RunContext;

/**
 * Common logic for an {@link IAccessControlService} implementation. An Implementation has to override
 * {@link #getCurrentUserCacheKey()} and {@link #execLoadPermissions(Object)}. For example use as generic key type
 * String and simply return as cache key the current userId in {@link #getCurrentUserCacheKey()}.
 * <p>
 * <b>Note</b> that the method {@link #execLoadPermissions(Object)} must not have a valid implementation in the client,
 * as a client will always get the value from the server. Therefore, consider two implementations like
 * <tt>'CustomAccessControlService'</tt> and <tt>'CustomServerAccessControlService'</tt>.
 * <p>
 * This class caches permission collections. As default, the cache is transactional and with a time to live duration of
 * one hour. To change any of these properties override {@link #createCacheBuilder()}.
 *
 * @param <K>
 *          the type of keys maintained the cache
 * @since 4.3.0 (Mars-M5)
 */
public abstract class AbstractAccessControlService<K> implements IAccessControlService {
  public static final String ACCESS_CONTROL_SERVICE_CACHE_ID = AbstractAccessControlService.class.getName();

  // never null
  private volatile Pattern[] m_userIdSearchPatterns;
  private volatile ICache<K, IPermissionCollection> m_cache;

  public AbstractAccessControlService() {
    m_userIdSearchPatterns = new Pattern[]{
        Pattern.compile("(.*)"),
    };
  }

  /**
   * Creates and initializes a new cache. Executed in {@link PostConstruct} to ensure that the cache created exactly
   * once.
   */
  @PostConstruct
  protected void initCache() {
    m_cache = createCacheBuilder().build();
  }

  /**
   * see {@link #setUserIdSearchPatterns(Pattern...)}
   */
  protected Pattern[] getUserIdSearchPatterns() {
    return m_userIdSearchPatterns;
  }

  /**
   * see {@link #setUserIdSearchPatterns(String...)}
   */
  protected void setUserIdSearchPatterns(Pattern... patterns) {
    // m_userIdSearchPatterns never null
    m_userIdSearchPatterns = Objects.requireNonNullElseGet(patterns, () -> new Pattern[]{});
  }

  /**
   * Set the pattern by which the userId is searched for in the list of jaas principal names.<br>
   * The first group of the pattern is assumed to be the username.<br>
   * By default, the following patterns are applied in this order:
   * <ul>
   * <li>".*"
   * </ul>
   */
  protected void setUserIdSearchPatterns(String... patterns) {
    Pattern[] a = new Pattern[patterns.length];
    for (int i = 0; i < a.length; i++) {
      a[i] = Pattern.compile(patterns[i]);
    }
    setUserIdSearchPatterns(a);
  }

  /**
   * Can be overridden to customize the cache builder
   *
   * @return {@link ICacheBuilder} for the internal cache
   */
  protected ICacheBuilder<K, IPermissionCollection> createCacheBuilder() {
    @SuppressWarnings("unchecked")
    ICacheBuilder<K, IPermissionCollection> cacheBuilder = BEANS.get(ICacheBuilder.class);
    return cacheBuilder.withCacheId(ACCESS_CONTROL_SERVICE_CACHE_ID).withValueResolver(createCacheValueResolver())
        .withShared(true)
        .withClusterEnabled(true)
        .withTransactional(true)
        .withTransactionalFastForward(true)
        .withTimeToLive(1L, TimeUnit.HOURS, false);
  }

  protected ICacheValueResolver<K, IPermissionCollection> createCacheValueResolver() {
    return this::execLoadPermissions;
  }

  protected ICache<K, IPermissionCollection> getCache() {
    return m_cache;
  }

  /**
   * Implement this method to get the cache key of the current user. Extract it from the current session or any other
   * property in the current {@link RunContext}.
   *
   * @return cache key of the current user or null if the current context has no user assigned to it.
   */
  protected abstract K getCurrentUserCacheKey();

  /**
   * Implement this method to load a {@link PermissionCollection} for a given cache key. This method must be valid
   * <b>only</b> in the server. Client does never call this method but loads its value directly from the server cache.
   *
   * @return new PermissionCollection for the given cache key
   */
  protected abstract IPermissionCollection execLoadPermissions(K cacheKey);

  @Override
  public String getUserIdOfCurrentSubject() {
    return getUserId(Subject.getSubject(AccessController.getContext()));
  }

  @Override
  public String getUserId(Subject subject) {
    if (subject == null) {
      return null;
    }
    for (Principal p : subject.getPrincipals()) {
      String name = p.getName().toLowerCase();
      for (Pattern pat : m_userIdSearchPatterns) {
        Matcher m = pat.matcher(name);
        if (m.matches()) {
          return m.group(1);
        }
      }
    }
    return null;
  }

  @Override
  public IPermissionCollection getPermissions() {
    IPermissionCollection permissions = getCache().get(getCurrentUserCacheKey());
    return permissions == null ? BEANS.get(NonePermissionCollection.class) : permissions;
  }

  @Override
  public void clearCache() {
    getCache().invalidate(new AllCacheEntryFilter<>(), true);
  }

  @Override
  public void clearCacheOfCurrentUser() {
    clearCache(Collections.singleton(getCurrentUserCacheKey()));
  }

  protected void clearCache(Collection<? extends K> cacheKeys) {
    if (cacheKeys == null) {
      return;
    }
    KeyCacheEntryFilter<K, IPermissionCollection> filter = new KeyCacheEntryFilter<>(cacheKeys);
    if (filter.getKeys().isEmpty()) {
      return;
    }
    getCache().invalidate(filter, true);
  }
}
