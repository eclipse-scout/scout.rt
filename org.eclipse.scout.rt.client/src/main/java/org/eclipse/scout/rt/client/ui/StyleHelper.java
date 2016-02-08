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
package org.eclipse.scout.rt.client.ui;

import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Helper class providing functionality to modify the set of CSS classes.
 *
 * @since 5.2
 */
@ApplicationScoped
public class StyleHelper {

  /**
   * Adds a class to a given class string if not contained yet.
   */
  public String addCssClass(String cssClasses, String cssClass) {
    if (StringUtility.hasText(cssClasses)
        && StringUtility.hasText(cssClass)
        && !cssClasses.matches("(.* |^)" + Pattern.quote(cssClass) + "( .*|$)")) {
      return cssClasses + " " + cssClass;
    }
    else {
      return cssClasses;
    }
  }

  /**
   * Removes a class (all occurrences) from a given class string.
   */
  public String removeCssClass(String cssClasses, String cssClass) {
    String[] classes = StringUtility.split(cssClasses, " ");
    for (int i = 0; i < classes.length; i++) {
      if (CompareUtility.equals(classes[i], cssClass)) {
        classes[i] = null;
      }
    }
    return StringUtility.join(" ", classes);
  }

  /**
   * Toggles a class on a given class string.
   * <p>
   * Class is added for <code>add==true</code>. </br>
   * Otherwise the class is removed.
   */
  public String toggleCssClass(String cssClasses, String cssClass, boolean add) {
    return add ? addCssClass(cssClasses, cssClass) : removeCssClass(cssClasses, cssClass);
  }

  /**
   * Adds a class to a given {@link IStyleable} if not contained yet.
   */
  public void addCssClass(IStyleable stylable, String cssClass) {
    if (stylable != null) {
      stylable.setCssClass(addCssClass(stylable.getCssClass(), cssClass));
    }
  }

  /**
   * Removes a class (all occurrences) from a given {@link IStyleable}.
   */
  public void removeCssClass(IStyleable stylable, String cssClass) {
    if (stylable != null) {
      stylable.setCssClass(removeCssClass(stylable.getCssClass(), cssClass));
    }
  }

  /**
   * Toggles a class on a given {@link IStyleable}.
   * <p>
   * Class is added for <code>add==true</code>. </br>
   * Otherwise the class is removed.
   */
  public void toggleCssClass(IStyleable stylable, String cssClass, boolean add) {
    if (add) {
      addCssClass(stylable, cssClass);
    }
    else {
      removeCssClass(stylable, cssClass);
    }
  }
}
