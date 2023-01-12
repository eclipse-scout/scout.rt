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
 * HTML document (&lt;html&gt;)
 */
public interface IHtmlDocument extends IHtmlElement {

  String HTML5_DOCTYPE = "<!DOCTYPE html>";

  IHtmlDocument doctype(String type);

  /**
   * @return HTML document with HTML5 doctype {@value IHtmlDocument#HTML5_DOCTYPE}
   */
  IHtmlDocument doctype();
}
