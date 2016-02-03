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
package org.eclipse.scout.commons.html;

/**
 * A html element
 *
 * @since 6.0 (backported)
 */
public interface IHtmlElement extends CharSequence, IHtmlContent {

  /**
   * Add a css class
   */
  IHtmlElement cssClass(CharSequence cssClass);

  /**
   * Add a css style
   */
  IHtmlElement style(CharSequence style);

  /**
   * Add an application local link
   *
   * @param path
   *          path to identify what is the link referring to.
   */
  IHtmlElement appLink(CharSequence path);

  /**
   * Add a html attribute to the element.
   *
   * @param name
   *          of the attribute
   * @param value
   *          of the attribute
   */
  IHtmlElement addAttribute(String name, CharSequence value);
}
