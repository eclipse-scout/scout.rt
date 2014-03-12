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
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiUtility;

/**
 * An {@link IDocTextExtractor} for classes that extracts special descriptions
 * <p>
 * Special descriptions are DocTexts which are retrieved from the TextProviderService with the classId + a suffix as
 * key.
 * <p>
 * For example there are two special descriptions available for {@link AbstractStringField}:<br>
 * d8b1f73a-4415-4477-8408-e6ada9e69551_name<br>
 * d8b1f73a-4415-4477-8408-e6ada9e69551_description
 */
public class SpecialDescriptionExtractor extends AbstractNamedTextExtractor<Class> {

  protected String m_keySuffix;
  protected boolean m_createAnchor;

  /**
   * @param name
   * @param keySuffix
   * @param createAnchor
   *          if true the extracted text starts with an anchor with ["c_" + classId] as id
   */
  public SpecialDescriptionExtractor(String name, String keySuffix, boolean createAnchor) {
    super(name);
    m_keySuffix = keySuffix;
    m_createAnchor = createAnchor;
  }

  /**
   * convenience for {@link #SpecialDescriptionExtractor(name, keySuffix, false)}
   * 
   * @param name
   * @param keySuffix
   */
  public SpecialDescriptionExtractor(String name, String keySuffix) {
    this(name, keySuffix, false);
  }

  /**
   * @return fully qualified class name.
   */
  @Override
  public String getText(Class clazz) {
    String text = TEXTS.getWithFallback(ConfigurationUtility.getAnnotatedClassIdWithFallback(clazz) + m_keySuffix, null);
    if (text == null) {
      return null;
    }
    return m_createAnchor ? MediawikiUtility.createAnchor("c_" + ConfigurationUtility.getAnnotatedClassIdWithFallback(clazz)) + text : text;
  }

}
