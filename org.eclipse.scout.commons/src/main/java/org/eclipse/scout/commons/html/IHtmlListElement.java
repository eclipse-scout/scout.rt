/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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
 * Marker Interface for list element &lt;li&gt;text&lt;/li&gt
 */
public interface IHtmlListElement extends IHtmlElement {

  /**
   * Add a css class
   */
  @Override
  IHtmlListElement cssClass(CharSequence cssClass);

  /**
   * Add a css style
   */
  @Override
  IHtmlListElement style(CharSequence style);

  /**
   * Add an application local link
   *
   * @param path
   *          path to identify what is the link referring to.
   */
  @Override
  IHtmlListElement appLink(CharSequence path);

  /**
   * {@inheritDoc}
   */
  @Override
  IHtmlListElement addAttribute(String name, CharSequence value);
}
