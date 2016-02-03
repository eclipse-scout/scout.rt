/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.rap.basic;

import org.eclipse.scout.rt.client.ui.IHtmlCapable;

/**
 * @since 4.2
 */
public interface IRwtScoutHtmlValidator {

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

}
