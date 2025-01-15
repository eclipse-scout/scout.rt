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

/**
 * HTML element
 */
public interface IHtmlElement extends IHtmlContent {

  /**
   * Sets the 'class' attribute (CSS).
   */
  IHtmlElement cssClass(CharSequence cssClass);

  /**
   * Sets the 'class' attribute (CSS), or if the attribute is already set, adds the css class to the existing css
   * classes in the 'class' attribute.
   */
  IHtmlElement addCssClass(CharSequence cssClass);

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
   *     Reference to identify what the link is referring to
   */
  IHtmlElement appLink(CharSequence ref);

  /**
   * Add an application local link.
   *
   * @param ref
   *     Reference to identify what the link is referring to
   * @param cssClass
   *     CSS class to add to the generated link element
   */
  IHtmlElement appLink(CharSequence ref, CharSequence cssClass);

  /**
   * Add a HTML attribute to the element.
   *
   * @param name
   *     of the attribute
   * @param value
   *     of the attribute
   */
  IHtmlElement addAttribute(String name, CharSequence value);

  /**
   * Add a boolean HTML attribute to the element.
   *
   * @param name
   *     of the attribute
   * @see <a href="https://www.w3.org/TR/html5/infrastructure.html#boolean-attributes">HTML 5 spec - Boolean
   * attributes<a/>
   */
  IHtmlElement addBooleanAttribute(String name);
}
