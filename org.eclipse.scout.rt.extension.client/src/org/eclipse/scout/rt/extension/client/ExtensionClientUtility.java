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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.scout.commons.BeanUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * @since 3.9.0
 */
public final class ExtensionClientUtility {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ExtensionClientUtility.class);

  private ExtensionClientUtility() {
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
   * Removes those objects of the given list that match any {@link Replace} annotation on other elements being part of
   * the same list.
   */
  public static <T> void processReplaceAnnotations(List<T> list) {
    if (list == null || list.isEmpty()) {
      return;
    }

    // get instances replacing other instances (i.e. replacements)
    List<T> replacements = new ArrayList<T>();
    for (T t : list) {
      if (t == null) {
        continue;
      }

      if (t.getClass().isAnnotationPresent(Replace.class)) {
        replacements.add(t);
      }
    }

    if (replacements.isEmpty()) {
      // no replacements
      return;
    }

    for (T replacement : replacements) {
      Class<?> replacementClass = replacement.getClass();
      Replace replace = replacementClass.getAnnotation(Replace.class);
      Class classToBeReplaced = replace.value();
      if (classToBeReplaced == Object.class) {
        // replace superclass
        classToBeReplaced = replacementClass.getSuperclass();
      }
      if (classToBeReplaced == null || classToBeReplaced == Object.class || classToBeReplaced.isPrimitive()) {
        if (LOG.isInfoEnabled()) {
          LOG.info("invalid class to be replaced on [" + replacementClass + "], class to be replaced [" + classToBeReplaced + "]");
        }
        continue;
      }
      TreeMap<CompositeObject, T> candidates = new TreeMap<CompositeObject, T>();
      int counter = 0;
      for (T t : list) {
        if (t == replacement) {
          continue;
        }
        int score = BeanUtility.computeTypeDistance(classToBeReplaced, t.getClass());
        if (score == -1) {
          continue;
        }
        candidates.put(new CompositeObject(score, counter), t);
        counter++;
      }
      if (candidates.isEmpty()) {
        if (LOG.isInfoEnabled()) {
          LOG.info("no candidates found on replacement class [" + replacementClass + "], class to be replaced [" + classToBeReplaced + "]");
        }
        continue;
      }
      // get result and check quality
      Entry<CompositeObject, T> secondEntry = null;
      Iterator<Entry<CompositeObject, T>> iterator = candidates.entrySet().iterator();
      Entry<CompositeObject, T> firstEntry = iterator.next();
      if (iterator.hasNext()) {
        secondEntry = iterator.next();
      }
      // check quality
      if (secondEntry != null) {
        int firstScore = TypeCastUtility.castValue(((CompositeObject) firstEntry.getKey()).getComponent(0), int.class);
        int secondScore = TypeCastUtility.castValue(((CompositeObject) secondEntry.getKey()).getComponent(0), int.class);
        if (firstScore == secondScore) {
          LOG.warn("ambiguous original classes for replacement [" + classToBeReplaced + "], " +
              "candidates <" + firstEntry.getValue().getClass() + ", " + secondEntry.getValue().getClass() + ">");
        }
      }
      list.remove(firstEntry.getValue());
    }
  }
}
