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
package org.eclipse.scout.rt.platform.html.internal;

/**
 * Builder for a html link.
 */
public class HtmlLinkBuilder extends HtmlNodeBuilder {

  private static final long serialVersionUID = 1L;

  public HtmlLinkBuilder(CharSequence url, CharSequence text) {
    super("a", text);
    addAttribute("href", url);
  }
}
