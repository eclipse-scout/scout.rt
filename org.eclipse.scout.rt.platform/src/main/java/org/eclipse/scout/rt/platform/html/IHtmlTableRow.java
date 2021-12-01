/*
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
