/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.html.internal;

/**
 * Builder for a HTML image element (&lt;img&gt;).
 */
public class HtmlImageBuilder extends EmptyHtmlNodeBuilder {

  private static final long serialVersionUID = 1L;

  public HtmlImageBuilder(CharSequence path) {
    super("img");
    addAttribute("src", path);
    addAttribute("alt", "");
  }
}
