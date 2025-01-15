/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.html;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

/**
 * Helper for modifying CSS class strings.
 *
 * @since 8.0 (in rt.client since 5.2)
 */
@ApplicationScoped
public class StyleHelper {

  /**
   * Adds a class to a given class string if not contained yet.
   * <p>
   * Orders are preserved. The white space in the resulting string is normalized. Duplicates in the list of existing
   * classes are not removed, while duplicates from the list of classes to be added are removed.
   *
   * @param cssClasses
   *     Existing class string (consisting of one or more CSS classes separated by space). Classes are added to
   *     this string.
   * @param cssClassesToAdd
   *     Class string (consisting of one or more CSS classes separated by space) to add to the existing classes.
   * @return never <code>null</code>
   */
  public String addCssClass(String cssClasses, String cssClassesToAdd) {
    return addCssClasses(cssClasses, cssClassesAsList(cssClassesToAdd));
  }

  public String addCssClasses(String cssClasses, String... cssClassesToAdd) {
    return addCssClasses(cssClasses, Arrays.asList(cssClassesToAdd));
  }

  public String addCssClasses(String cssClasses, List<String> cssClassesToAdd) {
    List<String> existingCssClasses = cssClassesAsList(cssClasses);
    for (String candidate : cssClassesToAdd) {
      if (!existingCssClasses.contains(candidate)) {
        existingCssClasses.add(candidate);
      }
    }
    return CollectionUtility.format(existingCssClasses, " ");
  }

  /**
   * Removes all occurrences of a class from a given class string.
   * <p>
   * Orders are preserved. The white space in the resulting string is normalized. Duplicates in the list of existing
   * classes are not removed (except if they are contained in the list of classes to remove, in which case all
   * occurrences are removed).
   *
   * @param cssClasses
   *     Existing class string (consisting of one or more CSS classes separated by space). Classes are removed from
   *     this string.
   * @param cssClassToRemove
   *     Class string (consisting of one or more CSS classes separated by space) to remove from the existing
   *     classes.
   * @return never <code>null</code>
   */
  public String removeCssClass(String cssClasses, String cssClassToRemove) {
    return removeCssClasses(cssClasses, cssClassesAsList(cssClassToRemove));
  }

  public String removeCssClasses(String cssClasses, String... cssClassesToRemove) {
    return removeCssClasses(cssClasses, Arrays.asList(cssClassesToRemove));
  }

  public String removeCssClasses(String cssClasses, List<String> cssClassesToRemove) {
    List<String> existingCssClasses = cssClassesAsList(cssClasses);
    existingCssClasses.removeAll(cssClassesToRemove);
    return CollectionUtility.format(existingCssClasses, " ");
  }

  /**
   * Toggles a class on a given class string, i.e. adds or removes the class depending on the {@code condition} flag.
   *
   * @param cssClasses
   *     Existing class string (consisting of one or more CSS classes separated by space). Classes are added or
   *     removed from this string.
   * @param cssClass
   *     Class string (consisting of one or more CSS classes separated by space) to add or remove from the existing
   *     classes.
   * @param condition
   *     Class is added when this is <code>true</code>, otherwise it is removed.
   * @return never <code>null</code>
   * @see #addCssClass(String, String)
   * @see #removeCssClass(String, String)
   */
  public String toggleCssClass(String cssClasses, String cssClass, boolean condition) {
    return condition ? addCssClass(cssClasses, cssClass) : removeCssClass(cssClasses, cssClass);
  }

  /**
   * Checks if a class exists in a given class string.
   *
   * @param cssClasses
   *     Existing class string (consisting of one or more CSS classes separated by space). Existence is checked in
   *     this string.
   * @param cssClass
   *     Class string (consisting of one or more CSS classes separated by space) to find in the existing classes.
   * @return <code>true</code> if all of the given classes are contained in the list of existing classes.
   */
  public boolean hasCssClass(String cssClasses, String cssClass) {
    List<String> existingCssClasses = cssClassesAsList(cssClasses);
    List<String> cssClassesToCheck = cssClassesAsList(cssClass);
    if (cssClassesToCheck.isEmpty()) {
      return true;
    }
    if (existingCssClasses.isEmpty()) {
      return false;
    }
    return existingCssClasses.containsAll(cssClassesToCheck);
  }

  /**
   * Converts the space separated CSS class string to a list.
   * <p>
   * Order is preserved. White space is trimmed.
   *
   * @return never <code>null</code>
   */
  public List<String> cssClassesAsList(String cssClass) {
    List<String> cssClasses = new ArrayList<>();
    String cssClassesStr = ObjectUtility.nvl(cssClass, "").trim();
    if (cssClassesStr.length() > 0) {
      for (String s : cssClassesStr.split(" ")) {
        s = s.trim();
        if (s.length() > 0) {
          cssClasses.add(s);
        }
      }
    }
    return cssClasses;
  }
}
