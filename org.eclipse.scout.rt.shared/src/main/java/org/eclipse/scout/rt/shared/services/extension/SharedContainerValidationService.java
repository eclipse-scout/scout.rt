/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.extension;

import java.io.Serializable;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModel;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModelEntity;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelEntity;
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
    addPossibleContributionForContainer(IDataModelAttribute.class, AbstractDataModel.class);
    addPossibleContributionForContainer(IDataModelEntity.class, AbstractDataModel.class);
    addPossibleContributionForContainer(IDataModelAttribute.class, AbstractDataModelEntity.class);
    addPossibleContributionForContainer(IDataModelEntity.class, AbstractDataModelEntity.class);
    addPossibleContributionForContainer(ICode.class, AbstractCode.class);
    addPossibleContributionForContainer(ICode.class, AbstractCodeTypeWithGeneric.class);
    addPossibleContributionForContainer(Serializable.class, AbstractTableRowData.class); // for row data extensions serializable beans are allowed.

    // moves
    addPossibleMoveForContainer(ICode.class, ICode.class);
    addPossibleMoveForContainer(ICode.class, IMoveModelObjectToRootMarker.class);
  }
}
