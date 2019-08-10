/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui;

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

  default void addCssClasses(String... cssClasses) {
    setCssClass(BEANS.get(StyleHelper.class).addCssClasses(getCssClass(), cssClasses));
  }

  /**
   * @param cssClass
   *          one or more CSS classes separated by space
   */
  default void removeCssClass(String cssClass) {
    setCssClass(BEANS.get(StyleHelper.class).removeCssClass(getCssClass(), cssClass));
  }

  default void removeCssClasses(String... cssClasses) {
    setCssClass(BEANS.get(StyleHelper.class).removeCssClasses(getCssClass(), cssClasses));
  }

  default void toggleCssClass(String cssClass, boolean condition) {
    setCssClass(BEANS.get(StyleHelper.class).toggleCssClass(getCssClass(), cssClass, condition));
  }
}
