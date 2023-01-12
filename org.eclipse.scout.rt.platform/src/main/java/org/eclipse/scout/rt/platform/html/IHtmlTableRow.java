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
 * HTML table row (&lt;tr&gt;)
 */
public interface IHtmlTableRow extends IHtmlElement {

  @Override
  IHtmlTableRow cssClass(CharSequence cssClass);

  @Override
  IHtmlTableRow style(CharSequence style);

  @Override
  IHtmlTableRow appLink(CharSequence ref);

  @Override
  IHtmlTableRow addAttribute(String name, CharSequence value);
}
