/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.html.StyleHelper;

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
    setCssClass(BEANS.get(StyleHelper.class).addCssClass(getCssClass(), cssClass));
  }

  /**
   * @param cssClass
   *          one or more CSS classes separated by space
   */
  default void removeCssClass(String cssClass) {
    setCssClass(BEANS.get(StyleHelper.class).removeCssClass(getCssClass(), cssClass));
  }

  default void toggleCssClass(String cssClass, boolean condition) {
    setCssClass(BEANS.get(StyleHelper.class).toggleCssClass(getCssClass(), cssClass, condition));
  }

  /**
   * Converts the space separated CSS class string to a list.
   *
   * @deprecated will be removed in 10.0.x, use {@link StyleHelper#cssClassesAsList(String)} instead.
   */
  @Deprecated
  static List<String> cssClassesAsList(String cssClass) {
    return BEANS.get(StyleHelper.class).cssClassesAsList(cssClass);
  }
}
