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
package org.eclipse.scout.rt.platform.html;

public interface IHtmlInput extends IHtmlElement {

  String TYPE_TEXT = "text";
  String TYPE_PASSWORD = "password"; // NOSONAR
  String TYPE_CHECKBOX = "checkbox";
  String TYPE_RADIO = "radio";
  String TYPE_BUTTON = "button";
  String TYPE_HIDDEN = "hidden";
  String TYPE_NUMBER = "number";

  String TYPE_SUBMIT = "submit";
  String TYPE_RESET = "reset";

  IHtmlInput id(String id);

  IHtmlInput name(String name);

  IHtmlInput type(String type);

  IHtmlInput value(Object value);

  IHtmlInput maxlength(int maxlength);

  /**
   * Only to be used with input fields of type {@value #TYPE_CHECKBOX} and {@value #TYPE_RADIO}.
   */
  IHtmlInput checked();

  /**
   * {@inheritDoc}
   */
  @Override
  IHtmlInput cssClass(CharSequence cssClass);

  /**
   * {@inheritDoc}
   */
  @Override
  IHtmlInput style(CharSequence style);

  /**
   * {@inheritDoc}
   */
  @Override
  IHtmlInput appLink(CharSequence path);

  /**
   * {@inheritDoc}
   */
  @Override
  IHtmlInput addAttribute(String name, CharSequence value);
}
