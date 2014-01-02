/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.spec.client.gen.extract;

import org.eclipse.scout.rt.shared.TEXTS;

/**
 * Description for a boolean value
 */
public abstract class AbstractBooleanTextExtractor<T> extends AbstractNamedTextExtractor<T> {

  public static final String DOC_ID_TRUE = "org.eclipse.scout.rt.spec.true";
  public static final String DOC_ID_FALSE = "org.eclipse.scout.rt.spec.false";

  /**
   * @param name
   */
  public AbstractBooleanTextExtractor(String name) {
    super(name);
  }

  /**
   * Returns the language specific text for a boolean value
   * 
   * @param b
   * @return text
   */
  protected String getBooleanText(boolean b) {
    return b ? TEXTS.get(DOC_ID_TRUE) : TEXTS.get(DOC_ID_FALSE);
  }

}
