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
package org.eclipse.scout.rt.shared.security;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.BasicPermission;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.BooleanUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.collection.ConcurrentExpiringMap;
import org.eclipse.scout.rt.shared.SharedConfigProperties;
import org.eclipse.scout.rt.shared.SharedConfigProperties.PermissionLevelCheckCacheTimeToLiveProperty;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic abstract class for hierarchical permissions. It allows to defer calculation of a concrete permission level to a
 * special function. To use this you have to override {@link #execCalculateLevel(BasicHierarchyPermission)}.
 * <p>
 * For example you could imagine a ModifyCompanyPermission. This permission has an additional property
 * <tt>companyKey</tt> and it defines an additional level <tt>'OWN'</tt>. Now a user has in his permission collection
 * the level 'OWN' granted. The application wants to check
 * {@code ACCESS.check(new ModifyCompanyPermission(myCompanyKey))}. Internally, {@link #implies(Permission)} detects
 * that it has to calculate the level of the permission in test. If the implementation of
 * {@link #execCalculateLevel(BasicHierarchyPermission)} does not return an higher required permission level than 'OWN',
 * the access is granted.
 * <p>
 * With the property {@link SharedConfigProperties.PermissionLevelCheckCacheTimeToLiveProperty} a caching can be
 * configured in order to reduce the calls to {@link #execCalculateLevel(BasicHierarchyPermission)}.
 */
public abstract class BasicHierarchyPermission extends BasicPermission {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(BasicHierarchyPermission.class);

  public static final int LEVEL_UNDEFINED = -1;
  public static final int LEVEL_NONE = 0;
  public static final int LEVEL_ALL = 100;

  private volatile boolean m_readOnly;
  private volatile int m_level;
  // cache
  private final List<Integer> m_validLevels;
  // lazy created in order to ensure caches are only created if permission instance belongs to a PermissionCollection (else cache is not required)
  private transient volatile Map<BasicHierarchyPermission, Boolean> m_levelPermissionCheckCache;

  public BasicHierarchyPermission(String name) {
    this(name, LEVEL_UNDEFINED);
  }

  public BasicHierarchyPermission(String name, int level) {
    super(name);
    m_validLevels = buildLevelCache();
    setLevel(level);
  }

  private List<Integer> buildLevelCache() {
    TreeSet<Integer> set = new TreeSet<Integer>();
    Field[] f = getClass().getFields();
    for (int i = 0; i < f.length; i++) {
      int flags = f[i].getModifiers();
      if (Modifier.isStatic(flags) && Modifier.isFinal(flags) && f[i].getName().startsWith("LEVEL_")) {
        try {
          int value = f[i].getInt(null);
          if (set.contains(value)) {
            throw new IllegalArgumentException("level " + f[i].getName() + " has the same value (" + value + ") as another level");
          }
          set.add(value);
        }
        catch (Exception e) {
          throw new IllegalArgumentException("could not build internal level cache", e);
        }
      }
    }
    return new ArrayList<Integer>(set);
  }

  protected Map<BasicHierarchyPermission, Boolean> getLevelPermissionCheckCache() {
    if (m_levelPermissionCheckCache == null) {
      // note: in case of an unlucky timing the cache would be created twice and previously cached values
      // will get lost. We accept this in order to preserve performance / readability / transient
      m_levelPermissionCheckCache = createLevelPermissionCheckCache();
    }
    return m_levelPermissionCheckCache;
  }

  /**
   * @return a cache for recent level permission checks or null if no caching should be used
   */
  protected Map<BasicHierarchyPermission, Boolean> createLevelPermissionCheckCache() {
    Long timeToLiveDuration = CONFIG.getPropertyValue(PermissionLevelCheckCacheTimeToLiveProperty.class);
    if (timeToLiveDuration == null) {
      return null;
    }
    return new ConcurrentExpiringMap<BasicHierarchyPermission, Boolean>(timeToLiveDuration, TimeUnit.MILLISECONDS);
  }

  /**
   * array of available levels, starting with the lowest, ending with the highest
   */
  public final List<Integer> getValidLevels() {
    return CollectionUtility.arrayList(m_validLevels);
  }

  public final int getLevel() {
    return m_level;
  }

  public final void setLevel(int level) {
    if (m_readOnly) {
      throw new SecurityException("Permission is read-only");
    }
    if (!m_validLevels.contains(level)) {
      throw new IllegalArgumentException("invalid level: " + level);
    }
    m_level = level;
  }

  public final void setReadOnly() {
    m_readOnly = true;
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj) && this.m_level == ((BasicHierarchyPermission) obj).m_level;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + m_level;
    return result;
  }

  /**
   * @return false if permission <tt>p</tt> requires a greater permission level than this permission has (or super
   *         implementation returns false). If permission <tt>p</tt> has the level {@link #LEVEL_UNDEFINED} the level
   *         has to be calculated for the given permission <tt>p</tt>. By default
   *         {@link IAccessControlService#calculateBasicHierarchyPermissionLevel(BasicHierarchyPermission)} is called.
   *         Calculated levels may be cached (see {@link #createLevelPermissionCheckCache()}).
   */
  @Override
  public boolean implies(Permission p) {
    if (this.getClass().isAssignableFrom(p.getClass())) {
      BasicHierarchyPermission other = (BasicHierarchyPermission) p;
      if (super.implies(p)) {
        switch (this.m_level) {
          case LEVEL_ALL: {
            return true;
          }
          case LEVEL_UNDEFINED: {
            LOG.warn("The level of a " + this.getClass().getSimpleName() + " in the permission collection should not have the level LEVEL_UNDEFINED");
            return false;
          }
          case LEVEL_NONE: {
            return false;
          }
          default: {
            if (other.m_level == LEVEL_UNDEFINED) {
              return checkLevel(other);
            }
            else {
              return this.m_level >= other.m_level;
            }
          }
        }
      }
    }
    return false;
  }

  protected boolean checkLevel(BasicHierarchyPermission other) {
    Map<BasicHierarchyPermission, Boolean> cache = getLevelPermissionCheckCache();
    Boolean b = cache != null ? cache.get(other) : null;
    if (b == null) {
      int requiredLevel = execCalculateLevel(other);
      b = m_level >= requiredLevel;
      if (cache != null) {
        cache.put(other, b);
      }
    }
    return BooleanUtility.nvl(b);
  }

  /**
   * Called by {@link #implies(Permission)} when level of currently checking permission has value
   * {@link #LEVEL_UNDEFINED} to
   *
   * @param other
   *          permission for which level should be calculated
   * @return required level for the given permission that this permission should have
   */
  protected int execCalculateLevel(BasicHierarchyPermission other) {
    return LEVEL_ALL; // default implementation requires level ALL; Therefore permission is only granted if user has level ALL.
  }
}
