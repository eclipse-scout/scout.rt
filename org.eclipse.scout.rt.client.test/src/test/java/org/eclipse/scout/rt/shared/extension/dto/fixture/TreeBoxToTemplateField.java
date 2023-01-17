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

import java.util.Set;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBox;
import org.eclipse.scout.rt.platform.extension.Extends;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.extension.dto.fixture.AbstractTemplateBox.GroupBoxInTemplateField;

@Extends(GroupBoxInTemplateField.class)
@FormData(value = TreeBoxToTemplateFieldData.class, sdkCommand = FormData.SdkCommand.CREATE, defaultSubtypeSdkCommand = FormData.DefaultSubtypeSdkCommand.DEFAULT)
public class TreeBoxToTemplateField extends AbstractTreeBox<Integer> {

  public static final Set<Integer> LIST_BOX_DEFAULT_VAL = CollectionUtility.hashSet(1, 2, 3);

  @Override
  protected void execInitField() {
    super.execInitField();
    setValue(LIST_BOX_DEFAULT_VAL);
  }
}
