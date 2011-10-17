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
package org.eclipse.scout.rt.shared.services.common.workflow;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.validate.InputValidation;
import org.eclipse.scout.rt.shared.validate.IValidationStrategy;
import org.eclipse.scout.service.IService;

/**
 * This is the central handler for workflows and delegates to the appropriate
 * IWorkflowService on the server Clients call a proxy on this interface that
 * delegates to the backend. <br>
 * Servers create subclasses of AbstractWorkflowService, resp. implement
 * IWorkflowService to contribute their workflows
 * <p>
 * This interface is not intended to be implemented by third party
 */
@Priority(-3)
@InputValidation(IValidationStrategy.PROCESS.class)
public interface IWorkflowProviderService extends IService {

  /**
   * @param filter
   *          Standard sql searchFilter
   * @return specs for all available workflow types that can be used in {@link #create(T)} and {@link #resume(Long)}
   */
  @InputValidation(IValidationStrategy.QUERY.class)
  AbstractWorkflowData[] getAvailableWorkflowTypes(SearchFilter filter) throws ProcessingException;

  /**
   * @param filter
   *          Standard sql searchFilter
   * @return filtered workflows accessible in the current user session
   */
  @InputValidation(IValidationStrategy.QUERY.class)
  AbstractWorkflowData[] getFilteredWorkflows(SearchFilter filter) throws ProcessingException;

  /**
   * Create and start a new workflow based on the specification <br>
   * Normally either {@link T#getDefinitionNr()} is set to identify the workflow
   * type or the data type itself uniquely identifies the workflow type
   * 
   * @return new workflow or null in case workflow starts asynchronously
   */
  <T extends AbstractWorkflowData> T create(T spec) throws ProcessingException;

  /**
   * Resume an already existing workflow based on the specification <br>
   * At least {@link T#getWorkflowNr()} must be set to identify the existing
   * workflow
   * 
   * @return resumed workflow or null in case workflow resumes asynchronously
   */
  <T extends AbstractWorkflowData> T resume(T spec) throws ProcessingException;

  /**
   * Make state transition in workflow, that means that the next step is marked
   * as the new current step <br>
   * Detailed: If the current step is not yet completed, it is kept as current
   * step, otherwise the next step is activated
   * 
   * @return workflow in new state
   */
  <T extends AbstractWorkflowData> T makeStateTransition(T data) throws ProcessingException;

  /**
   * Store the workflow data and all its step datas
   */
  <T extends AbstractWorkflowData> T store(T data) throws ProcessingException;

  /**
   * Finish and finalize workflow
   * 
   * @return workflow in finished state
   */
  <T extends AbstractWorkflowData> T finish(T data) throws ProcessingException;

  /**
   * Discard and finalize workflow
   * 
   * @return workflow in discarded state
   */
  <T extends AbstractWorkflowData> T discard(T data) throws ProcessingException;

}
