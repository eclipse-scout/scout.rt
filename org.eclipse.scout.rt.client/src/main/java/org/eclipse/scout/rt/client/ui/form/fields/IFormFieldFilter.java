/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields;

import org.eclipse.scout.rt.client.ui.form.AbstractForm;

/**
 * filters form fields
 */
@FunctionalInterface
public interface IFormFieldFilter {
  /**
   * specifies if the given field should be further processed.
   *
   * @param field
   *          The field to check.
   * @return true if the given field should be processed. false otherwise.
   * @see {@link AbstractForm#importFormData(org.eclipse.scout.rt.shared.data.form.AbstractFormData, boolean, org.eclipse.scout.rt.platform.reflect.IPropertyFilter, IFormFieldFilter)}
   */
  boolean accept(IFormField field);
}
