/*
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.services.extension;

import javax.annotation.PostConstruct;

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
