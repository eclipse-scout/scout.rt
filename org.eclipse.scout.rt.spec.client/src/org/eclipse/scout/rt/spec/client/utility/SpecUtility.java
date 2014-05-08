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
package org.eclipse.scout.rt.spec.client.utility;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.osgi.BundleInspector;
import org.eclipse.scout.commons.osgi.BundleInspector.IClassFilter;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.spec.client.gen.extract.SpecialDescriptionExtractor;

/**
 * General utilities for the spec plugin
 */
public final class SpecUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SpecUtility.class);
  private static Set<Class<?>> s_allClasses;
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
   * wrapper around {@link BundleInspector#getAllClasses(IClassFilter)} which caches all classes on first invocation
   * 
   * @param filter
   * @return
   * @throws ProcessingException
   */
  public static Set<Class<?>> getAllClasses(IClassFilter filter) throws ProcessingException {
    if (s_allClasses == null) {
      s_allClasses = BundleInspector.getAllClasses(new IClassFilter() {
        @Override
        public boolean accept(Class c) {
          return true;
        }
      });
    }
    HashSet<Class<?>> filteredClasses = new HashSet<Class<?>>();
    for (Class c : s_allClasses) {
      if (filter.accept(c)) {
        filteredClasses.add(c);
      }
    }
    return filteredClasses;
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
  public static List<IMenu> expandMenuHierarchy(List<IMenu> menus) {
    ArrayList<IMenu> menuList = new ArrayList<IMenu>();
    for (IMenu menu : menus) {
      addMenuRecursive(menuList, menu);
    }
    return menuList;
  }

  private static void addMenuRecursive(ArrayList<IMenu> menuList, IMenu menu) {
    menuList.add(menu);
    for (IMenu subMenu : menu.getChildActions()) {
      addMenuRecursive(menuList, subMenu);
    }
  }

  /**
   * @return all doc entity classes (forms, pages, ...) in all available bundles
   * @throws ProcessingException
   */
  public static Set<Class<?>> getAllDocEntityClasses() throws ProcessingException {
    return getAllClasses(new IClassFilter() {
      // TODO ASA accept other types that needs to be linked like [[CompanyForm|Company]
      @Override
      public boolean accept(Class c) {
        return IForm.class.isAssignableFrom(c) || IPage.class.isAssignableFrom(c);
      }
    });
  }

  /**
   * A <code>type</code> is considered a documented type if the following criterias are met:
   * <p>
   * <li>Instances of the type can be assigned to the <code>supertype</code>.
   * <li>Either the type is annotated with a {@link ClassId} annotation for which a doc-text with key
   * <code>[classid]_name</code> is available or <code>listTypesWithoutDoc</code> is set to true.
   * 
   * @param type
   * @param supertype
   * @param listTypesWithoutDoc
   * @return
   */
  public static boolean isDocType(Class type, Class<?> supertype, boolean listTypesWithoutDoc) {
    if (type == null || !supertype.isAssignableFrom(type)) {
      return false;
    }
    if (listTypesWithoutDoc) {
      return !type.isInterface() && !Modifier.isAbstract(type.getModifiers());
    }
    String typeDescription = new SpecialDescriptionExtractor(null, "_name").getText(type);
    return typeDescription != null;
  }

}
