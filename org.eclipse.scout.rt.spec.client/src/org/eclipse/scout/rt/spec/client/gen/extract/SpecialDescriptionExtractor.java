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

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.rt.shared.TEXTS;

/**
 * An {@link IDocTextExtractor} with the fully qualified class name as text.
 */
public class SpecialDescriptionExtractor extends AbstractNamedTextExtractor<Class> implements IDocTextExtractor<Class> {

  private String m_keySuffix;

  /**
   * @param string
   */
  public SpecialDescriptionExtractor(String name, String keySuffix) {
    super(name);
    m_keySuffix = keySuffix;
  }

  /**
   * @return fully qualified class name.
   */
  @Override
  public String getText(Class clazz) {
    String doc = TEXTS.get(ConfigurationUtility.getAnnotatedClassIdWithFallback(clazz) + m_keySuffix);
    // TODO ASA fix this hack: name.contains("{undefined text")
    if (doc.startsWith("{undefined text")) {
      return null;
    }
    return doc;
  }

}
