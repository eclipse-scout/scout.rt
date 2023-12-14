/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.extension;

import java.io.Serializable;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;
import org.eclipse.scout.rt.shared.extension.AbstractContainerValidationService;
import org.eclipse.scout.rt.shared.extension.IMoveModelObjectToRootMarker;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCode;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeTypeWithGeneric;
import org.eclipse.scout.rt.shared.services.common.code.ICode;

@Order(5200)
public class SharedContainerValidationService extends AbstractContainerValidationService {

  @PostConstruct
  protected void initializeContributions() {
    // contributions
    addPossibleContributionForContainer(AbstractFormFieldData.class, AbstractFormData.class);
    addPossibleContributionForContainer(AbstractPropertyData.class, AbstractFormData.class);
    addPossibleContributionForContainer(AbstractFormFieldData.class, AbstractFormFieldData.class);
    addPossibleContributionForContainer(AbstractPropertyData.class, AbstractFormFieldData.class);
    addPossibleContributionForContainer(ICode.class, AbstractCode.class);
    addPossibleContributionForContainer(ICode.class, AbstractCodeTypeWithGeneric.class);
    addPossibleContributionForContainer(Serializable.class, AbstractTableRowData.class); // for row data extensions serializable beans are allowed.

    // moves
    addPossibleMoveForContainer(ICode.class, ICode.class);
    addPossibleMoveForContainer(ICode.class, IMoveModelObjectToRootMarker.class);
  }
}
