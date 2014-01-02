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
import org.eclipse.scout.rt.spec.client.gen.extract.AbstractBooleanTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;

/**
 * Extracts the value of a boolean property for form fields.
 * ({@link AbstractBooleanTextExtractor#DOC_ID_TRUE} or {@link AbstractBooleanTextExtractor#DOC_ID_FALSE}.
 */
public class FormFieldBooleanPropertyExtractor extends AbstractBooleanTextExtractor<IFormField> implements IDocTextExtractor<IFormField> {
  private final String m_propertyName;

  public FormFieldBooleanPropertyExtractor(String propertyName, String header) {
    super(header);
    m_propertyName = propertyName;

  }

  /**
   * Reads the property of the form field and returns the translated doc text for {@value #DOC_ID_TRUE}, if the property
   * is <code>true</code>. {@value #DOC_ID_FALSE} otherwise.
   */
  @Override
  public String getText(IFormField field) {
    Object property = field.getProperty(m_propertyName);
    boolean b = Boolean.parseBoolean(StringUtility.nvl(property, "false"));
    return getBooleanText(b);
  }

}
