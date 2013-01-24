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

import org.eclipse.scout.commons.StringUtility;

/**
 * @since 3.8.0
 */
public final class FormDataUtility {

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
      return s.replaceAll("Field$", "");
    }
    if (s.endsWith("Button")) {
      return s.replaceAll("Button$", "");
    }
    return s;
  }
}
