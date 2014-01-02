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
package org.eclipse.scout.rt.spec.client.gen.extract.action;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.spec.client.gen.extract.AbstractNamedTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;

/**
 * Extracts the value of a property
 */
public class ActionPropertyExtractor<T extends IAction> extends AbstractNamedTextExtractor<T> implements IDocTextExtractor<T> {
  private final String m_propertyName;

  /**
   * @param propertyName
   *          the name of the property (e.g. {@link IAction#PROP_TEXT}
   * @param header
   *          a header for the extractor
   */
  public ActionPropertyExtractor(String propertyName, String header) {
    super(header);
    m_propertyName = propertyName;
  }

  /**
   * Reads the property of the action and returns its value converted to a String
   */
  @Override
  public String getText(T object) {
    return StringUtility.nvl(object.getProperty(m_propertyName), "");
  }

}
