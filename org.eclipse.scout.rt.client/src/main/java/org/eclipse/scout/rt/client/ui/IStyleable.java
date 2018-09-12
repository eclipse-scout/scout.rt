/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

/**
 * @since 5.1
 */
public interface IStyleable {
  String PROP_CSS_CLASS = "cssClass";

  String getCssClass();

  void setCssClass(String cssClass);

  /**
   * @param cssClass
   *          one or more CSS classes separated by space
   */
  default void addCssClass(String cssClass) {
    List<String> cssClasses = cssClassesAsList(cssClass);
    List<String> existingCssClasses = cssClassesAsList(getCssClass());
    for (String cssClassStr : cssClasses) {
      if (existingCssClasses.indexOf(cssClassStr) >= 0) {
        continue;
      }
      existingCssClasses.add(cssClassStr);
    }
    setCssClass(CollectionUtility.format(existingCssClasses, " "));
  }

  /**
   * @param cssClass
   *          one or more CSS classes separated by space
   */
  default void removeCssClass(String cssClass) {
    List<String> cssClasses = cssClassesAsList(cssClass);
    List<String> existingCssClasses = cssClassesAsList(getCssClass());
    if (existingCssClasses.removeAll(cssClasses)) {
      setCssClass(CollectionUtility.format(existingCssClasses, " "));
    }
  }

  default void toggleCssClass(String cssClass, boolean condition) {
    if (condition) {
      addCssClass(cssClass);
    }
    else {
      removeCssClass(cssClass);
    }
  }

  /**
   * Converts the space separated CSS class string to a list.
   */
  static List<String> cssClassesAsList(String cssClass) {
    List<String> cssClasses = new ArrayList<>();
    String cssClassesStr = ObjectUtility.nvl(cssClass, "");

    cssClassesStr = cssClassesStr.trim();
    if (cssClassesStr.length() > 0) {
      cssClasses = CollectionUtility.arrayList(cssClassesStr.split(" "));
    }
    return cssClasses;
  }
}
