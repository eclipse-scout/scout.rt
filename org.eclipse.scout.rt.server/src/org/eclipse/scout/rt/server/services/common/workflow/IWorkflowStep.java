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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.workflow.AbstractWorkflowData;
import org.eclipse.scout.rt.shared.services.common.workflow.AbstractWorkflowStepData;

/**
 * A WorkflowStep is a stateless(!) processor of a step in a workflow
 */
public interface IWorkflowStep<T extends AbstractWorkflowStepData> {

  /**
   * Prepare the step. This method is called when a state transition has entered
   * this step When prepare fails, no save is performed
   */
  void prepareStep(AbstractWorkflowData data, T stepData, boolean hasNextStep) throws ProcessingException;

  /**
   * Complete the step by setting {@link AbstractWorkflowStepData#setCompleteDate()} This method is called
   * when a state transition is leaving this step
   */
  void completeStep(AbstractWorkflowData data, T stepData, boolean hasNextStep) throws ProcessingException;

  /**
   * set default properties on AbstractWorkflowData object
   * definitionNr,definitionText,definitionActive,definitionDoc, ...
   */
  void assignDefinitions(T data);

}
