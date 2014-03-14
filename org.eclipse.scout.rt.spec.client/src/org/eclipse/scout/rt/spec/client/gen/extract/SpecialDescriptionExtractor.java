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
  protected IDocTextExtractor<Class> m_fallback;

  /**
   * @param name
   * @param keySuffix
   * @param createAnchor
   *          If true the extracted text starts with an anchor with ["c_" + classId] as id.
   * @param fallback
   *          Used by {@link #getText(Class)} when no special description is available.
   */
  public SpecialDescriptionExtractor(String name, String keySuffix, boolean createAnchor, IDocTextExtractor<Class> fallback) {
    super(name);
    m_keySuffix = keySuffix;
    m_createAnchor = createAnchor;
    m_fallback = fallback;
  }

  /**
   * convenience for {@link #SpecialDescriptionExtractor(String, String, boolean, IDocTextExtractor)} with parameters
   * <code>(name, keySuffix, createAnchor, null)</code>
   * 
   * @param name
   * @param keySuffix
   * @param createAnchor
   */
  public SpecialDescriptionExtractor(String name, String keySuffix, boolean createAnchor) {
    this(name, keySuffix, createAnchor, null);
  }

  /**
   * convenience for {@link #SpecialDescriptionExtractor(String, String, boolean, IDocTextExtractor)} with parameters
   * <code>(name, keySuffix, false, null)</code>
   * 
   * @param name
   * @param keySuffix
   */
  public SpecialDescriptionExtractor(String name, String keySuffix) {
    this(name, keySuffix, false, null);
  }

  /**
   * @return special description or fallback.
   *         <p>
   *         Optionally the returned text contains an anchor.
   *         <p>
   *         Null is returned if no special description is available and fallback is either not set or extracts null.
   */
  @Override
  public String getText(Class clazz) {
    String text = TEXTS.getWithFallback(ConfigurationUtility.getAnnotatedClassIdWithFallback(clazz) + m_keySuffix, null);
    if (text == null && m_fallback != null) {
      text = m_fallback.getText(clazz);
    }
    return text != null && m_createAnchor ? MediawikiUtility.createAnchor("c_" + ConfigurationUtility.getAnnotatedClassIdWithFallback(clazz)) + text : text;
  }

}
