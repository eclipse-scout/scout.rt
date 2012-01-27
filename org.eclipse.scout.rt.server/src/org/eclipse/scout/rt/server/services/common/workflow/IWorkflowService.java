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
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.workflow.AbstractWorkflowData;
import org.eclipse.scout.service.IService;

/**
 * A Workflow is a stateless(!) processor of workflow state machine
 */
public interface IWorkflowService<T extends AbstractWorkflowData> extends IService {

  IWorkflowStep[] getSteps();

  <S extends IWorkflowStep> S getStepByClass(Class<S> stepClass);

  /**
   * @return step if it is contained in the workflow step list, null otherwise
   */
  <S extends IWorkflowStep> S resolveStep(S step);

  /**
   * @param filter
   *          Standard sql searchFilter
   * @return specs for all available workflow types that can be used in {@link #create(T)} and {@link #resume(T)}
   */
  AbstractWorkflowData[] getAvailableWorkflowTypes(SearchFilter filter) throws ProcessingException;

  /**
   * @param filter
   *          Standard sql searchFilter
   * @return filtered workflows accessible in the current user session
   */
  AbstractWorkflowData[] getFilteredWorkflows(SearchFilter filter) throws ProcessingException;

  /**
   * Create and start a new workflow based on the specification Normally either {@link T#getDefinitionNr()} is set to
   * identify the workflow type or the
   * data type itself uniquely identifies the workflow type
   * 
   * @return new workflow or null in case workflow starts asynchronously
   */
  T create(T spec) throws ProcessingException;

  T store(T data) throws ProcessingException;

  /**
   * Resume an already existing workflow based on the specification At least {@link T#getWorkflowNr()} must be set to
   * identify the existing workflow
   * 
   * @return resumed workflow or null in case workflow resumes asynchronously
   */
  T resume(T spec) throws ProcessingException;

  /**
   * Make state transition in workflow, that means that the next step is marked
   * as the new current step by calling {@link AbstractWorkflowData#advanceStepDataIndex()} Detailed: If the
   * current step is not yet completed, it is kept as current step, otherwise
   * the next step is activated
   * 
   * @return workflow in new state
   */
  T makeStateTransition(T data) throws ProcessingException;

  /**
   * Finish and finalize workflow
   * 
   * @return workflow in finished state
   */
  T finish(T data) throws ProcessingException;

  /**
   * Discard and finalize workflow
   * 
   * @return workflow in discarded state
   */
  T discard(T data) throws ProcessingException;

}
