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

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModel;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModelEntity;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelEntity;
import org.eclipse.scout.rt.shared.extension.AbstractContainerValidationService;

@Order(5300)
public class DataModelSharedContainerValidationService extends AbstractContainerValidationService {

  @PostConstruct
  protected void initializeContributions() {
    // contributions
    addPossibleContributionForContainer(IDataModelAttribute.class, AbstractDataModel.class);
    addPossibleContributionForContainer(IDataModelEntity.class, AbstractDataModel.class);
    addPossibleContributionForContainer(IDataModelAttribute.class, AbstractDataModelEntity.class);
    addPossibleContributionForContainer(IDataModelEntity.class, AbstractDataModelEntity.class);
  }
}
