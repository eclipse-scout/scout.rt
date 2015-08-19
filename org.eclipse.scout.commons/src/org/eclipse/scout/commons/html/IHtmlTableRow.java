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
 * Marker Interface for table row
 */
public interface IHtmlTableRow extends IHtmlElement {

  /**
   * Add a css class
   */
  @Override
  IHtmlTableRow cssClass(CharSequence cssClass);

  /**
   * Add a css style
   */
  @Override
  IHtmlTableRow style(CharSequence style);

  /**
   * Add an application local link
   *
   * @param path
   *          path to identify what is the link referring to.
   */
  @Override
  IHtmlTableRow appLink(CharSequence path);

  /**
   * {@inheritDoc}
   */
  @Override
  IHtmlTableRow addAttribute(String name, CharSequence value);
}
