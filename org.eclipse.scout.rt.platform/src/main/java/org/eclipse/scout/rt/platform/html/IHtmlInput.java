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
 * HTML input element (&lt;input&gt;)
 */
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

  @Override
  IHtmlInput cssClass(CharSequence cssClass);

  @Override
  IHtmlInput style(CharSequence style);

  @Override
  IHtmlInput appLink(CharSequence path);

  @Override
  IHtmlInput addAttribute(String name, CharSequence value);
}
