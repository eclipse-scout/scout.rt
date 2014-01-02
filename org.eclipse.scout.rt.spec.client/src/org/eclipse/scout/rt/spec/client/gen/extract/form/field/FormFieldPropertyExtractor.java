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
package org.eclipse.scout.rt.spec.client.gen.extract.form.field;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.spec.client.gen.extract.AbstractNamedTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;

/**
 * Extracts the value of a property
 */
public class FormFieldPropertyExtractor extends AbstractNamedTextExtractor<IFormField> implements IDocTextExtractor<IFormField> {
  private final String m_propertyName;

  /**
   * @param propertyName
   *          the name of the property (e.g. {@link IFormField#PROP_TEXT}
   * @param header
   *          a header for the extractor
   */
  public FormFieldPropertyExtractor(String propertyName, String header) {
    super(header);
    m_propertyName = propertyName;
  }

  /**
   * Reads the property of the form field and returns its value
   */
  @Override
  public String getText(IFormField field) {
    return StringUtility.nvl(field.getProperty(m_propertyName), "");
  }

}
