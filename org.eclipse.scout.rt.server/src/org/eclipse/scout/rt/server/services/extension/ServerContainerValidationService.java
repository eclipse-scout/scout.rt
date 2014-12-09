/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.extension;

import org.eclipse.scout.rt.server.services.common.workflow.AbstractWorkflowService;
import org.eclipse.scout.rt.server.services.common.workflow.IWorkflowStep;
import org.eclipse.scout.rt.shared.extension.AbstractContainerValidationService;
import org.osgi.framework.ServiceRegistration;

/**
 *
 */
public class ServerContainerValidationService extends AbstractContainerValidationService {
  @Override
  public void initializeService(ServiceRegistration registration) {
    super.initializeService(registration);
    addPossibleContributionForContainer(IWorkflowStep.class, AbstractWorkflowService.class);
  }
}
