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

public interface IHtmlInput extends IHtmlElement {

  IHtmlInput id(String id);

  IHtmlInput name(String name);

  IHtmlInput type(String type);

  IHtmlInput value(String value);

  IHtmlInput maxlength(int maxlength);

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
}
