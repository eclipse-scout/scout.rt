/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.security;

import static org.eclipse.scout.rt.platform.util.Assertions.assertTrue;

import java.security.PermissionCollection;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.Subject;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.cache.AbstractCacheWrapper;
import org.eclipse.scout.rt.platform.cache.AllCacheEntryFilter;
import org.eclipse.scout.rt.platform.cache.ICache;
import org.eclipse.scout.rt.platform.cache.ICacheBuilder;
import org.eclipse.scout.rt.platform.cache.ICacheEntryFilter;
import org.eclipse.scout.rt.platform.cache.ICacheInvalidationListener;
import org.eclipse.scout.rt.platform.cache.ICacheValueResolver;
import org.eclipse.scout.rt.platform.cache.KeyCacheEntryFilter;
import org.eclipse.scout.rt.platform.security.User;
import org.eclipse.scout.rt.platform.util.event.FastListenerList;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common logic for an {@link IAccessControlService} implementation. An Implementation has to override
 * {@link #execLoadPermissions(User)}. The method {{@link #getUser(Subject)}} creates the {@link User} object for the
 * given {@link Subject} that is used to load the permissions and is also used as cache key.
 * <p>
 * <b>Note</b> that the method {@link #execLoadPermissions(User)} must not have a valid implementation in the client,
 * as a client will always get the value from the server. Therefore, consider two implementations like
 * <tt>'CustomAccessControlService'</tt> and <tt>'CustomServerAccessControlService'</tt>.
 * <p>
 * This class caches permission collections. As default, the cache is transactional and with a time to live duration of
 * one hour. To change any of these properties override {@link #createCacheBuilder()}.
 *
 * @since 4.3.0 (Mars-M5)
 */
public abstract class AbstractAccessControlService implements IAccessControlService {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractAccessControlService.class);
  public static final String ACCESS_CONTROL_SERVICE_CACHE_ID = AbstractAccessControlService.class.getName();

  private volatile Pattern[] m_userIdSearchPatterns;
  private volatile ICache<User, IPermissionCollection> m_cache;
  private volatile IFastListenerList<ICacheInvalidationListener<User, IPermissionCollection>> m_invalidationListeners;

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
    m_invalidationListeners = new FastListenerList<>();
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
  protected ICacheBuilder<User, IPermissionCollection> createCacheBuilder() {
    @SuppressWarnings("unchecked")
    ICacheBuilder<User, IPermissionCollection> cacheBuilder = BEANS.get(ICacheBuilder.class);
    return cacheBuilder.withCacheId(ACCESS_CONTROL_SERVICE_CACHE_ID).withValueResolver(createCacheValueResolver())
        .withShared(true)
        .withClusterEnabled(true)
        .withTransactional(true)
        .withTransactionalFastForward(true)
        .withAdditionalCustomWrapper(InvalidationListenerWrapper.class)
        .withTimeToLive(1L, TimeUnit.HOURS, false);
  }

  protected static class InvalidationListenerWrapper extends AbstractCacheWrapper<User, IPermissionCollection> {

    public InvalidationListenerWrapper(ICache<User, IPermissionCollection> delegate) {
      super(delegate);
    }

    @Override
    public void invalidate(ICacheEntryFilter<User, IPermissionCollection> filter, boolean propagate) {
      super.invalidate(filter, propagate);
      BEANS.get(IAccessControlService.class).getInvalidationListeners().forEach(l -> l.invalidated(filter, propagate));
    }
  }

  @Override
  public void addInvalidationListener(ICacheInvalidationListener<User, IPermissionCollection> listener) {
    if (listener != null) {
      m_invalidationListeners.add(listener);
    }
  }

  @Override
  public void removeInvalidationListener(ICacheInvalidationListener<User, IPermissionCollection> listener) {
    if (listener != null) {
      m_invalidationListeners.remove(listener);
    }
  }

  @Override
  public List<ICacheInvalidationListener<User, IPermissionCollection>> getInvalidationListeners() {
    return m_invalidationListeners.list();
  }

  protected ICacheValueResolver<User, IPermissionCollection> createCacheValueResolver() {
    return user -> {
      assertTrue(user.isReadOnly(), "User must be read only before accessing the permission cache");
      return execLoadPermissions(user);
    };
  }

  protected ICache<User, IPermissionCollection> getCache() {
    return m_cache;
  }

  /**
   * Implement this method to load a {@link PermissionCollection} for a given cache key. This method must be valid
   * <b>only</b> in the server. Client does never call this method but loads its value directly from the server cache.
   *
   * @return new PermissionCollection for the given cache key
   */
  protected abstract IPermissionCollection execLoadPermissions(User user);

  @Override
  public User getUser(Subject subject) {
    if (subject == null) {
      return null;
    }
    User user = BEANS.get(User.class)
        .withUserId(extractUserId(subject));
    initUser(user, subject);
    return user.setReadOnly();
  }

  @Override
  public String extractUserId(Subject subject) {
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

  /**
   * Load additional data using given {@code subject} to {@link User}.
   */
  protected void initUser(User user, Subject subject) {
    // NOP
  }

  @Override
  public IPermissionCollection getPermissions() {
    User user = User.current();
    IPermissionCollection permissions = getCache().get(user);
    LOG.trace("getPermissions() called for {}, returned {}", user, permissions);
    if (permissions == null) {
      LOG.error("getPermissions() called for {}, returned {}", user, permissions);
    }
    return permissions == null ? BEANS.get(NonePermissionCollection.class) : permissions;
  }

  @Override
  public void clearCache() {
    getCache().invalidate(new AllCacheEntryFilter<>(), true);
  }

  @Override
  public void clearCacheOfCurrentUser() {
    clearCache(Collections.singleton(User.current()));
  }

  protected void clearCache(Collection<User> cacheKeys) {
    if (cacheKeys == null) {
      return;
    }
    KeyCacheEntryFilter<User, IPermissionCollection> filter = new KeyCacheEntryFilter<>(cacheKeys);
    if (filter.getKeys().isEmpty()) {
      return;
    }
    getCache().invalidate(filter, true);
  }
}
