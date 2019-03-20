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
package org.eclipse.scout.rt.platform.html;

/**
 * A HTML element.
 */
public interface IHtmlElement extends IHtmlContent {

  /**
   * Sets the 'class' attribute (CSS).
   */
  IHtmlElement cssClass(CharSequence cssClass);

  /**
   * Sets the 'style' attribute (CSS).
   */
  IHtmlElement style(CharSequence style);

  /**
   * Sets the 'id' attribute.
   */
  IHtmlElement id(CharSequence id);

  /**
   * Add an application local link.
   *
   * @param ref
   *          Reference to identify what the link is referring to
   */
  IHtmlElement appLink(CharSequence ref);

  /**
   * Add an application local link.
   *
   * @param ref
   *          Reference to identify what the link is referring to
   * @param cssClass
   */
  IHtmlElement appLink(CharSequence ref, CharSequence cssClass);

  /**
   * Add a HTML attribute to the element.
   *
   * @param name
   *          of the attribute
   * @param value
   *          of the attribute
   */
  IHtmlElement addAttribute(String name, CharSequence value);

  /**
   * Add a boolean HTML attribute to the element.
   *
   * @param name
   *          of the attribute
   * @see <a href="https://www.w3.org/TR/html5/infrastructure.html#boolean-attributes">HTML 5 spec - Boolean
   *      attributes<a/>
   */
  IHtmlElement addBooleanAttribute(String name);

}
