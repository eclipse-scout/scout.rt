/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui;

import java.util.List;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;

/**
 * Helper class providing functionality to modify the set of CSS classes.
 *
 * @deprecated Code was moved to platform, use {@link org.eclipse.scout.rt.platform.html.StyleHelper} instead or methods
 *             on {@link IStyleable}. This class will be removed in 10.0.x.
 * @since 5.2
 */
@ApplicationScoped
@Deprecated
public class StyleHelper {

  /**
   * Adds a class to a given class string if not contained yet.
   *
   * @param cssClass
   *          one or more CSS classes separated by space
   */
  public String addCssClass(String cssClasses, String cssClass) {
    return BEANS.get(org.eclipse.scout.rt.platform.html.StyleHelper.class).addCssClass(cssClasses, cssClass);
  }

  /**
   * Removes a class (all occurrences) from a given class string.
   */
  public String removeCssClass(String cssClasses, String cssClass) {
    return BEANS.get(org.eclipse.scout.rt.platform.html.StyleHelper.class).removeCssClass(cssClasses, cssClass);
  }

  /**
   * Toggles a class on a given class string.
   * <p>
   * Class is added for <code>add==true</code>. </br>
   * Otherwise the class is removed.
   */
  public String toggleCssClass(String cssClasses, String cssClass, boolean condition) {
    return BEANS.get(org.eclipse.scout.rt.platform.html.StyleHelper.class).toggleCssClass(cssClasses, cssClass, condition);
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
  public void toggleCssClass(IStyleable stylable, String cssClass, boolean condition) {
    if (condition) {
      addCssClass(stylable, cssClass);
    }
    else {
      removeCssClass(stylable, cssClass);
    }
  }

  public boolean hasCssClass(String cssClasses, String cssClass) {
    return BEANS.get(org.eclipse.scout.rt.platform.html.StyleHelper.class).hasCssClass(cssClasses, cssClass);
  }

  /**
   * Converts the space separated CSS class string to a list.
   */
  public List<String> cssClassesAsList(String cssClass) {
    return BEANS.get(org.eclipse.scout.rt.platform.html.StyleHelper.class).cssClassesAsList(cssClass);
  }
}
