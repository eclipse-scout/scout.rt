/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.extension.client;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * @since 3.9.0
 */
public final class ExtensionUtility {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ExtensionUtility.class);

  private ExtensionUtility() {
  }

  /**
   * Removes all objects from the given list that are exact instance of the java types provided. An object <em>o</em> is
   * an exact instance of a class <em>C</em>, iff <code>o.getClass() == C</code>. i.e. instances of sub classes of
   * <em>C</em> are not exact instances of <em>C</em>.
   * 
   * @param objectList
   *          the list of classes to remove types from. The list is modified in general.
   * @param types
   *          vararg with exact types to be removed.
   */
  public static void removeByType(List<?> objectList, Class<?>... types) {
    if (objectList == null || types == null || types.length == 0) {
      return;
    }

    for (Iterator<?> it = objectList.iterator(); it.hasNext();) {
      Object next = it.next();
      if (next != null && CompareUtility.isOneOf(next.getClass(), (Object[]) types)) {
        it.remove();
      }
    }
  }

  /**
   * Computes the enclosing object for the given object. The enclosing object is the corresponding to
   * {@link Class#getEnclosingClass()}, but for instances.
   * 
   * @param o
   *          the object to get the enclosing object for
   * @return the enclosing object or <code>null</code> if the given object is <code>null</code>, if it is a primary
   *         class or if it is embedded into a static context.
   */
  public static Object getEnclosingObject(Object o) {
    if (o == null) {
      return null;
    }
    int nestedCount = o.getClass().getName().replaceAll("[^$]", "").trim().length();
    if (nestedCount == 0) {
      return null;
    }
    Object enclosingObject = null;
    try {
      Field f = o.getClass().getDeclaredField("this$" + (nestedCount - 1));
      f.setAccessible(true);
      enclosingObject = f.get(o);
    }
    catch (Throwable t) {
      // nop
    }
    return enclosingObject;
  }

  /**
   * Computes the first enclosing object on the given object's enclosing object path that implements the given type.
   * 
   * @param o
   *          the object to get the enclosing object for
   * @param type
   *          the expected type of the enclosing object
   * @return the enclosing object or <code>null</code> if the given object is <code>null</code>, if it is a primary
   *         class or if it is embedded into a static context.
   */
  public static Object getEnclosingObject(Object o, Class<?> type) {
    Object enclosingObject = getEnclosingObject(o);
    if (type != null) {
      while (enclosingObject != null && !type.isInstance(enclosingObject)) {
        enclosingObject = getEnclosingObject(enclosingObject);
      }
    }
    return enclosingObject;
  }
}
