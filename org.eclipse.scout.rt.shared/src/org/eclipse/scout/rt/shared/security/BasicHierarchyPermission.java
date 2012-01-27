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
package org.eclipse.scout.rt.shared.security;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.BasicPermission;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.service.SERVICES;

public abstract class BasicHierarchyPermission extends BasicPermission {
  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BasicHierarchyPermission.class);

  public static final int LEVEL_UNDEFINED = -1;
  public static final int LEVEL_NONE = 0;
  public static final int LEVEL_ALL = 100;
  private static long cacheTimeout = 60000L;

  public static long getCacheTimeoutMillis() {
    return cacheTimeout;
  }

  public static void setCacheTimeoutMillis(long t) {
    cacheTimeout = t;
  }

  private boolean m_readOnly;
  private int m_level;
  // cache
  private List<Integer> m_validLevels;

  public BasicHierarchyPermission(String name) {
    this(name, LEVEL_UNDEFINED);
  }

  public BasicHierarchyPermission(String name, int level) {
    super(name);
    buildLevelCache();
    setLevel(level);
  }

  @SuppressWarnings("boxing")
  private void buildLevelCache() {
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
    m_validLevels = new ArrayList<Integer>(set);
  }

  /**
   * array of available levels, starting with the lowest, ending with the
   * highest
   */
  public final List<Integer> getValidLevels() {
    return Collections.unmodifiableList(m_validLevels);
  }

  public final int getLevel() {
    return m_level;
  }

  @SuppressWarnings("boxing")
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
    if (obj instanceof BasicHierarchyPermission) {
      BasicHierarchyPermission other = (BasicHierarchyPermission) obj;
      if (this.m_level == other.m_level) {
        if (super.equals(obj)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode() ^ m_level;
  }

  protected String getConfiguredId() {
    return null;
  }

  /**
   * when p.level has value {@link #LEVEL_UNDEFINED} and also {@link #calculateLevel(int)} returns #
   * {@link #LEVEL_UNDEFINED} then set
   * p.level to the maximum of its level
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
              if (checkLevel(other, this.m_level)) {
                return true;
              }
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

  @SuppressWarnings("boxing")
  private boolean checkLevel(BasicHierarchyPermission other, int level) {
    // check if we are in the backend
    if (SERVICES.getService(IAccessControlService.class).isProxyService()) {
      throw new FineGrainedAccessCheckRequiredException();
    }
    try {
      boolean b = other.execCheckLevel(level);
      return b;
    }
    catch (ProcessingException e) {
      throw new SecurityException(e);
    }
  }

  /**
   * Only called in the backend. Frontend uses proxy cache. Called by {@link #implies(Permission)} when level has value
   * #LEVEL_UNDEFINED
   * 
   * @param requiredLevel
   *          default implementation calls {@link #execCheckLevelData(int)} and
   *          returns true if data yields rows and first rows first value is 1
   */
  @SuppressWarnings("boxing")
  protected boolean execCheckLevel(int requiredLevel) throws ProcessingException {
    Object[][] data = execCheckLevelData(requiredLevel);
    return data != null && data.length > 0 && TypeCastUtility.castValue(data[0][0], Boolean.class);
  }

  /**
   * called by {@link #implies(Permission)} via execCheckLevel when level has
   * value #LEVEL_UNDEFINED
   * 
   * @param requiredLevel
   * @return data with data[0][0]=1 as true
   */
  protected Object[][] execCheckLevelData(int requiredLevel) throws ProcessingException {
    return null;
  }

}
