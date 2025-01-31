/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
   *     The field to check.
   * @return true if the given field should be processed. false otherwise.
   * @see {@link AbstractForm#importFormData(org.eclipse.scout.rt.shared.data.form.AbstractFormData, boolean, org.eclipse.scout.rt.platform.reflect.IPropertyFilter, IFormFieldFilter)}
   */
  boolean accept(IFormField field);
}
