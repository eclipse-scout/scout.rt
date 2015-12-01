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
package org.eclipse.scout.rt.shared.data.form;

import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;

/**
 * @since 3.8.0
 */
public final class FormDataUtility {

  private static final Pattern FIELD_SUFFIX_PATTERN = Pattern.compile("Field$");
  private static final Pattern BUTTON_SUFFIX_PATTERN = Pattern.compile("Button$");
  private static final Pattern DATA_SUFFIX_PATTERN = Pattern.compile("Data$");

  private FormDataUtility() {
  }

  /**
   * Computes the field data ID for a given form field ID.
   * <p/>
   * <b>Note:</b> This method behaves exactly the same as the generate FormData operation in Scout SDK.
   *
   * @return Returns the corresponding field data ID for the given form field ID. The result is <code>null</code> if the
   *         field ID is <code>null</code> or if it contains white spaces only.
   */
  public static String getFieldDataId(String formFieldId) {
    String s = StringUtility.trim(formFieldId);
    if (StringUtility.isNullOrEmpty(s)) {
      return null;
    }
    if (s.endsWith("Field")) {
      return FIELD_SUFFIX_PATTERN.matcher(s).replaceAll("");
    }
    if (s.endsWith("Button")) {
      return BUTTON_SUFFIX_PATTERN.matcher(s).replaceAll("");
    }
    return s;
  }

  public static String getFieldDataId(AbstractFormFieldData fieldData) {
    String s = fieldData.getFieldId();
    if (s != null && s.endsWith("Data")) {
      s = DATA_SUFFIX_PATTERN.matcher(s).replaceAll("");
    }
    return getFieldDataId(s);
  }
}
