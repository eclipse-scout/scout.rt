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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.osgi.BundleInspector;
import org.eclipse.scout.commons.osgi.BundleInspector.IClassFilter;

/**
 * General utilities for the spec plugin
 */
public final class SpecUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SpecUtility.class);
  private static Set<Class> s_allClasses;

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

}
