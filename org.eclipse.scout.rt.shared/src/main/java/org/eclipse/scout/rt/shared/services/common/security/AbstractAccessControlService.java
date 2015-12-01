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
package org.eclipse.scout.rt.shared.services.common.security;

import java.security.AccessController;
import java.security.AllPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.cache.AbstractCacheValueResolver;
import org.eclipse.scout.rt.shared.cache.AllCacheEntryFilter;
import org.eclipse.scout.rt.shared.cache.ICache;
import org.eclipse.scout.rt.shared.cache.ICacheBuilder;
import org.eclipse.scout.rt.shared.cache.ICacheValueResolver;
import org.eclipse.scout.rt.shared.cache.KeyCacheEntryFilter;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;
import org.eclipse.scout.rt.shared.servicetunnel.LenientPermissionsWrapper;

/**
 * Common logic for an {@link IAccessControlService} implementation. An Implementation has to override
 * {@link #getCurrentUserCacheKey()} and {@link #execLoadPermissions(Object)}. For example use as generic key
 * type String and simply return as cache key the current userId in {@link #getCurrentUserCacheKey()}. You may use
 * {@link #getUserIdOfCurrentUser()} for this purpose.
 * <p>
 * <b>Note</b> that the method {@link #execLoadPermissions(Object)} must not have a valid implementation in the
 * client, as a client will always get the value from the server. Therefore consider two implementations like
 * <tt>'CustomAccessControlService'</tt> and <tt>'CustomServerAccessControlService'</tt>.
 * <p>
 * This class caches the permissions as a default transactional and with a time to live duration of one hour. To change
 * any of these properties override {@link #createCacheBuilder()}.
 *
 * @param <K>
 *          the type of keys maintained the cache
 * @since 4.3.0 (Mars-M5)
 */
public abstract class AbstractAccessControlService<K> implements IAccessControlService {
  public static final String ACCESS_CONTROL_SERVICE_CACHE_ID = AbstractAccessControlService.class.getName();

  // never null
  private volatile Pattern[] m_userIdSearchPatterns;
  private final ICache<K, PermissionCollection> m_cache;

  public AbstractAccessControlService() {
    m_cache = createCacheBuilder().build();
    m_userIdSearchPatterns = new Pattern[]{
        Pattern.compile(".*\\\\([^/@]+)"),
        Pattern.compile(".*\\\\([^/@]+)[/@].*"),
        Pattern.compile("([^/@]+)"),
        Pattern.compile("([^/@]+)[/@].*"),
    };
  }

  /**
   * see {@link #setUserIdSearchPatterns(Pattern...)}
   */
  protected Pattern[] getUserIdSearchPatterns() {
    return m_userIdSearchPatterns;
  }

  /**
   * see {@link #setUserIdSearchPatterns(Pattern...)}
   */
  protected void setUserIdSearchPatterns(Pattern... patterns) {
    if (patterns == null) {
      // m_userIdSearchPatterns never null
      m_userIdSearchPatterns = new Pattern[]{};
    }
    else {
      m_userIdSearchPatterns = patterns;
    }
  }

  /**
   * Set the pattern by which the userId is searched for in the list of jaas principal names.<br>
   * The first group of the pattern is assumed to be the username.<br>
   * By default the following patterns are applied in this order:
   * <ul>
   * <li>".*\\\\([^/@]+)" matching "DOMAIN\\user" to "user"
   * <li>".*\\\\([^/@]+)[/@].*" matching "DOMAIN\\user@domain.com" to "user"
   * <li>"([^/@]+)" matching "user" to "user"
   * <li>"([^/@]+)[/@].*" matching "user@domain.com" to "user"
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
  protected ICacheBuilder<K, PermissionCollection> createCacheBuilder() {
    @SuppressWarnings("unchecked")
    ICacheBuilder<K, PermissionCollection> cacheBuilder = BEANS.get(ICacheBuilder.class);
    return cacheBuilder.withCacheId(ACCESS_CONTROL_SERVICE_CACHE_ID).withValueResolver(createCacheValueResolver())
        .withShared(true)
        .withClusterEnabled(true)
        .withTransactional(true)
        .withTransactionalFastForward(true)
        .withTimeToLive(1L, TimeUnit.HOURS, false);
  }

  protected ICacheValueResolver<K, PermissionCollection> createCacheValueResolver() {
    return new AbstractCacheValueResolver<K, PermissionCollection>() {

      @Override
      public PermissionCollection resolve(K key) throws Exception {
        return execLoadPermissions(key);
      }
    };
  }

  protected ICache<K, PermissionCollection> getCache() {
    return m_cache;
  }

  /**
   * Implement this method to get the cache key of the current user. Extract it from the current {@link ISession} or any
   * other property in the current {@link RunContext}.
   * <p>
   * You may use the predefined method {@link #getUserIdOfCurrentUser()} if you use userId as a cache key.
   *
   * @return cache key of the current user or null it the current context has no user assigned to it.
   */
  protected abstract K getCurrentUserCacheKey();

  /**
   * Implement this method to load a {@link PermissionCollection} for a given cache key. This method must be valid
   * <b>only</b> in the server. Client does never call this method but loads its value directly from the server cache.
   * <p>
   * Note: In order to use automatically {@link LenientPermissionsWrapper} you should use {@link Permissions} as the
   * implementation of a {@link PermissionCollection}.
   *
   * @return new PermissionCollection for the given cache key
   */
  protected abstract PermissionCollection execLoadPermissions(K cacheKey);

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

  /**
   * Tries first to get UserId from session. If no active session can be found, {@link #getUserIdOfCurrentSubject()} is
   * called.
   *
   * @return current UserId or null if UserId can not be extracted from current {@link RunContext} and {@link Subject}
   */
  public String getUserIdOfCurrentUser() {
    ISession session = ISession.CURRENT.get();
    if (session != null && session.isActive()) {
      // only an active session has a valid userId
      return session.getUserId();
    }
    else {
      return getUserIdOfCurrentSubject();
    }
  }

  @Override
  public boolean checkPermission(Permission p) {
    if (p == null) {
      return true;
    }
    PermissionCollection c = getPermissions();
    if (c == null) {
      return true;
    }
    else {
      return c.implies(p);
    }
  }

  @Override
  public int getPermissionLevel(Permission p) {
    if (p == null) {
      return BasicHierarchyPermission.LEVEL_NONE;
    }
    if (!(p instanceof BasicHierarchyPermission)) {
      if (checkPermission(p)) {
        return BasicHierarchyPermission.LEVEL_ALL;
      }
      else {
        return BasicHierarchyPermission.LEVEL_NONE;
      }
    }
    BasicHierarchyPermission hp = (BasicHierarchyPermission) p;
    PermissionCollection c = getPermissions();
    if (c == null) {
      List<Integer> levels = hp.getValidLevels();
      return levels.get(levels.size() - 1);
    }
    else {
      int maxLevel = BasicHierarchyPermission.LEVEL_UNDEFINED;
      Enumeration<Permission> en = c.elements();
      while (en.hasMoreElements()) {
        Permission grantedPermission = en.nextElement();

        // catch AllPermission
        if (grantedPermission instanceof AllPermission) {
          return BasicHierarchyPermission.LEVEL_ALL;
        }

        // process basic hierarchy permissions
        if (grantedPermission instanceof BasicHierarchyPermission) {
          BasicHierarchyPermission hgrantedPermission = (BasicHierarchyPermission) grantedPermission;
          if (hgrantedPermission.getClass().isAssignableFrom(hp.getClass())) {
            maxLevel = Math.max(maxLevel, hgrantedPermission.getLevel());
            if (maxLevel >= BasicHierarchyPermission.LEVEL_ALL) {
              break;
            }
          }
        }
      }
      return maxLevel;
    }
  }

  @Override
  public PermissionCollection getPermissions() {
    return getCache().get(getCurrentUserCacheKey());
  }

  @Override
  public void clearCache() throws ProcessingException {
    getCache().invalidate(new AllCacheEntryFilter<K, PermissionCollection>(), true);
  }

  @Override
  public void clearCacheOfCurrentUser() throws ProcessingException {
    clearCache(Collections.singleton(getCurrentUserCacheKey()));
  }

  protected void clearCache(Collection<? extends K> cacheKeys) throws ProcessingException {
    if (cacheKeys != null && !cacheKeys.isEmpty()) {
      getCache().invalidate(new KeyCacheEntryFilter<K, PermissionCollection>(cacheKeys), true);
    }
  }
}
