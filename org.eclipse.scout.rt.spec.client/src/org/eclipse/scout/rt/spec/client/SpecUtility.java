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
package org.eclipse.scout.rt.spec.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.osgi.BundleInspector;
import org.eclipse.scout.commons.osgi.BundleInspector.IClassFilter;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.spec.client.config.DefaultDocConfig;
import org.eclipse.scout.rt.spec.client.config.IDocConfig;

/**
 * General utilities for the spec plugin
 */
public final class SpecUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SpecUtility.class);
  private static Set<Class> s_allClasses;
  private static IDocConfig s_docConfigInstance;

  private SpecUtility() {
  }

  /**
   * creates a base spec file name (without file extension) for a object
   * 
   * @param object
   * @return
   */
  public static String getSpecFileBaseName(ITypeWithClassId object) {
    return object.getClass().getSimpleName() + "_" + object.classId();
  }

  /**
   * creates a base file name (without file extension) for a class using it's annotated classId or the classId fallback
   * 
   * @param c
   * @return
   */
  public static String getSpecFileBaseName(Class c) {
    return c.getSimpleName() + "_" + ConfigurationUtility.getAnnotatedClassIdWithFallback(c);
  }

  /**
   * wrapper around {@link BundleInspector#getAllClasses(IClassFilter)} which caches all classes on first invocation
   * 
   * @param filter
   * @return
   * @throws ProcessingException
   */
  public static Set<Class> getAllClasses(IClassFilter filter) throws ProcessingException {
    if (s_allClasses == null) {
      s_allClasses = BundleInspector.getAllClasses(new IClassFilter() {
        @Override
        public boolean accept(Class c) {
          return true;
        }
      });
    }
    HashSet<Class> filteredClasses = new HashSet<Class>();
    for (Class c : s_allClasses) {
      if (filter.accept(c)) {
        filteredClasses.add(c);
      }
    }
    return filteredClasses;
  }

  /**
   * @return the {@link IDocConfig} instance
   */
  public static IDocConfig getDocConfigInstance() {
    if (s_docConfigInstance == null) {
      s_docConfigInstance = new DefaultDocConfig();
    }
    return s_docConfigInstance;
  }

  public static void setDocConfig(IDocConfig specFileConfig) {
    SpecUtility.s_docConfigInstance = specFileConfig;
  }

  /**
   * Create an anchorId using the object's classId with {@link #createAnchorId(String)}
   * 
   * @param object
   * @return
   */
  public static String createAnchorId(ITypeWithClassId object) {
    String classId = object.classId();
    return SpecUtility.createAnchorId(classId);
  }

  /**
   * Prepends the classId with "c_" to make sure the anchor does not start with a digit, which is forbidden for IDs in
   * HTML.
   * 
   * @param classId
   * @return
   */
  public static String createAnchorId(String classId) {
    return "c_" + classId;
  }

  /**
   * Creates a flat array with all menus by traversing the menu hierarchy by depth-first search.
   * 
   * @param menus
   *          the list of top level menus
   * @return
   */
  public static IMenu[] expandMenuHierarchy(IMenu[] menus) {
    ArrayList<IMenu> menuList = new ArrayList<IMenu>();
    for (IMenu menu : menus) {
      addMenuRecursive(menuList, menu);
    }
    return menuList.toArray(new IMenu[menuList.size()]);
  }

  private static void addMenuRecursive(ArrayList<IMenu> menuList, IMenu menu) {
    menuList.add(menu);
    for (IMenu subMenu : menu.getChildActions()) {
      addMenuRecursive(menuList, subMenu);
    }
  }

}
