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
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;
import org.eclipse.scout.rt.shared.services.common.code.CODES;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.spec.client.out.mediawiki.MediawikiUtility;

/**
 * Extractor for the name of CodeTypes prefixed with an anchor with the format: c_[classId]
 * <p>
 * The name of CodeTypes corresponds to the return value of a {@link AbstractCodeType#getText()}. If
 * {@link AbstractCodeType#getText()} returns null or an empty String the CodeType's simple class name is extract.
 */
public class CodeTypeNameExtractor extends AbstractNamedTextExtractor<Class<?>> {

  protected boolean m_createAnchor;

  /**
   * @param createAnchor
   *          If true the extracted text starts with an anchor with ["c_" + classId] as id.
   */
  public CodeTypeNameExtractor(boolean createAnchor) {
    super(TEXTS.get("org.eclipse.scout.rt.spec.type"));
    m_createAnchor = createAnchor;
  }

  @SuppressWarnings("unchecked")
  @Override
  public String getText(Class c) {
    if (!ICodeType.class.isAssignableFrom(c)) {
      // TODO ASA scout 4.0: can generic param be further typed Class<? extends ICodeType>?
      return null;
    }
    String text = CODES.getCodeType((Class<ICodeType<?, ?>>) c).getText();
    if (StringUtility.isNullOrEmpty(text)) {
      text = c.getSimpleName();
    }
    return text != null && m_createAnchor ? MediawikiUtility.createAnchor("c_" + ConfigurationUtility.getAnnotatedClassIdWithFallback(c)) + text : text;
  }
}
