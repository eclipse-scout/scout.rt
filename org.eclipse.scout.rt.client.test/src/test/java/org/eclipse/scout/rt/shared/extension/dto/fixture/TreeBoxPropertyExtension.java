/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.extension.dto.fixture;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension;

@Data(TreeBoxPropertyExtensionData.class)
public class TreeBoxPropertyExtension extends AbstractFormFieldExtension<TreeBoxToTemplateField> {

  private Long m_longValue;

  public TreeBoxPropertyExtension(TreeBoxToTemplateField owner) {
    super(owner);
  }

  @Data
  public Long getLongValue() {
    return m_longValue;
  }

  @Data
  public void setLongValue(Long value) {
    m_longValue = value;
  }
}
