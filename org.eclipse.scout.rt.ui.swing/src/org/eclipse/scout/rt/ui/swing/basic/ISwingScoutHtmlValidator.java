/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.basic;

import javax.swing.JLabel;

import org.eclipse.scout.rt.client.ui.IHtmlCapable;

/**
 * @since 4.2
 */
public interface ISwingScoutHtmlValidator {

  /**
   * Escape / sanitize a given HTML text on a given object of type {@link IHtmlCapable}.
   *
   * @param text
   *          the HTML text to be escaped / sanitized
   * @param htmlCapable
   *          the object of type {@link IHtmlCapable}
   * @return the escaped / sanitized text
   */
  String escape(String text, IHtmlCapable htmlCapable);

  /**
   * Removes the HTML renderer from a given {@link JLabel} if the given text contains HTML and if the given object is of
   * type {@link IHtmlCapable} that is not configured to enable HTML.
   *
   * @param obj
   *          the object of type {@link IHtmlCapable}
   * @param text
   *          the text that might contain HTML
   * @param label
   *          the {@link JLabel} where the HTML renderer should be removed from
   * @return {@code true} if the HTML is removed, {@code false} otherwise
   */
  boolean removeHtmlRenderer(Object obj, String text, JLabel label);

}
