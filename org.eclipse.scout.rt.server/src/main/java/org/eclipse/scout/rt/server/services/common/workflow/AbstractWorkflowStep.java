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
package org.eclipse.scout.rt.server.services.common.workflow;

import java.util.Date;

import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.workflow.AbstractWorkflowData;
import org.eclipse.scout.rt.shared.services.common.workflow.AbstractWorkflowStepData;

public abstract class AbstractWorkflowStep<T extends AbstractWorkflowStepData> implements IWorkflowStep<T> {

  public AbstractWorkflowStep() {
    initConfig();
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.TEXT)
  @Order(50)
  protected String getConfiguredDefinitionText() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(60)
  protected boolean getConfiguredDefinitionActive() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(10)
  protected boolean getConfiguredFinishPossible() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(20)
  protected boolean getConfiguredCancelPossible() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(30)
  protected boolean getConfiguredSuspendPossible() {
    return true;
  }

  @ConfigOperation
  @Order(10)
  protected void execPrepareStep(AbstractWorkflowData data, T stepData, boolean hasNextStep) throws ProcessingException {
  }

  @ConfigOperation
  @Order(20)
  protected void execCompleteStep(AbstractWorkflowData data, T stepData, boolean hasNextStep) throws ProcessingException {
    stepData.setCompletionDate(new Date());
  }

  protected void initConfig() {
  }

  @Override
  public void assignDefinitions(T data) {
    if (data != null) {
      data.setDefinitionText(getConfiguredDefinitionText());
      data.setDefinitionActive(getConfiguredDefinitionActive());
      data.setFinishPossible(getConfiguredFinishPossible());
      data.setCancelPossible(getConfiguredCancelPossible());
      data.setSuspendPossible(getConfiguredSuspendPossible());
    }
  }

  @Override
  public void prepareStep(AbstractWorkflowData data, T stepData, boolean hasNextStep) throws ProcessingException {
    execPrepareStep(data, stepData, hasNextStep);
  }

  @Override
  public void completeStep(AbstractWorkflowData data, T stepData, boolean hasNextStep) throws ProcessingException {
    execCompleteStep(data, stepData, hasNextStep);
  }

}
