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
import java.security.Permissions;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;
import org.eclipse.scout.rt.shared.services.common.security.internal.AccessControlStore;
import org.eclipse.scout.service.AbstractService;
import org.osgi.framework.ServiceRegistration;

/**
 * Common logic for {@link IAccessControlService} implementations.
 * Implementations should override {@link #execLoadPermissions()}
 *
 * @since 4.3.0 (Mars-M5)
 */
@Priority(-1)
public abstract class AbstractSharedAccessControlService extends AbstractService implements IAccessControlService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractSharedAccessControlService.class);

  private AccessControlStore m_accessControlStore;
  private Pattern[] m_userIdSearchPatterns;

  public AbstractSharedAccessControlService() {
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
    m_userIdSearchPatterns = patterns;
  }

  /**
   * Set the pattern by which the userId is searched for in the list of jaas
   * principal names.<br>
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

  @Override
  public String getUserIdOfCurrentSubject() {
    Subject s = Subject.getSubject(AccessController.getContext());
    if (s == null) {
      return null;
    }
    if (m_userIdSearchPatterns == null) {
      return null;
    }
    for (Principal p : s.getPrincipals()) {
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
  public void initializeService(ServiceRegistration registration) {
    m_accessControlStore = new AccessControlStore();
    super.initializeService(registration);
  }

  @Override
  public boolean checkPermission(Permission p) {
    if (p == null) {
      return true;
    }
    Permissions c = getPermissions();
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
    Permissions c = getPermissions();
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
  public Permissions getPermissions() {
    Permissions permSet = m_accessControlStore.getPermissionsOfCurrentSubject();

    if (permSet != null) {
      return permSet;
    }

    setPermissions(execLoadPermissions());
    permSet = m_accessControlStore.getPermissionsOfCurrentSubject();

    return permSet;
  }

  /**
   * This default implementation does nothing
   * Override this method to retrieve permissions from a custom store
   */
  protected Permissions execLoadPermissions() {
    return null;
  }

  private void setPermissions(Permissions p) {
    m_accessControlStore.setPermissionsOfCurrentSubject(p);

    notifySetPermisions(p);
  }

  /**
   * This methods is called during {@link #setPermissions(Permissions)}. It can be used by child implementations to
   * notify clients or clusters.
   *
   * @param permissions
   */
  protected void notifySetPermisions(Permissions permissions) {
  }

  @Override
  public boolean isProxyService() {
    return false;
  }

  @Override
  public void clearCache() {
    clearCacheNoFire();
  }

  @Override
  public void clearCacheOfUserIds(Collection<String> userIds) {
    clearCacheOfUserIdsNoFire(userIds);
  }

  protected void clearCacheNoFire() {
    m_accessControlStore.clearCache();
  }

  protected void clearCacheOfUserIdsNoFire(Collection<String> userIds) {
    m_accessControlStore.clearCacheOfUserIds(userIds);
  }
}
