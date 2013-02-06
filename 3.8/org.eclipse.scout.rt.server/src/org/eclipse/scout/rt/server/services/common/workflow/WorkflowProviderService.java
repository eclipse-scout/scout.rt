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

import java.util.ArrayList;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.workflow.AbstractWorkflowData;
import org.eclipse.scout.rt.shared.services.common.workflow.IWorkflowProviderService;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.SERVICES;

@Priority(-1)
public class WorkflowProviderService extends AbstractService implements IWorkflowProviderService {

  @Override
  public AbstractWorkflowData[] getAvailableWorkflowTypes(SearchFilter filter) throws ProcessingException {
    ArrayList<AbstractWorkflowData> list = new ArrayList<AbstractWorkflowData>();
    for (IWorkflowService s : SERVICES.getServices(IWorkflowService.class)) {
      for (AbstractWorkflowData data : s.getAvailableWorkflowTypes(filter)) {
        list.add(data);
      }
    }
    return list.toArray(new AbstractWorkflowData[list.size()]);
  }

  @Override
  public AbstractWorkflowData[] getFilteredWorkflows(SearchFilter filter) throws ProcessingException {
    ArrayList<AbstractWorkflowData> list = new ArrayList<AbstractWorkflowData>();
    for (IWorkflowService s : SERVICES.getServices(IWorkflowService.class)) {
      for (AbstractWorkflowData data : s.getFilteredWorkflows(filter)) {
        list.add(data);
      }
    }
    return list.toArray(new AbstractWorkflowData[list.size()]);
  }

  @Override
  public <T extends AbstractWorkflowData> T create(T spec) throws ProcessingException {
    return findWorkflowService(spec).create(spec);
  }

  @Override
  public <T extends AbstractWorkflowData> T discard(T data) throws ProcessingException {
    return findWorkflowService(data).discard(data);
  }

  @Override
  public <T extends AbstractWorkflowData> T makeStateTransition(T data) throws ProcessingException {
    return findWorkflowService(data).makeStateTransition(data);
  }

  @Override
  public <T extends AbstractWorkflowData> T store(T data) throws ProcessingException {
    return findWorkflowService(data).store(data);
  }

  @Override
  public <T extends AbstractWorkflowData> T finish(T data) throws ProcessingException {
    return findWorkflowService(data).finish(data);
  }

  @Override
  public <T extends AbstractWorkflowData> T resume(T spec) throws ProcessingException {
    return findWorkflowService(spec).resume(spec);
  }

  @SuppressWarnings("unchecked")
  private <T extends AbstractWorkflowData> IWorkflowService<T> findWorkflowService(T spec) throws ProcessingException {
    String className = spec.getDefinitionServiceClass();
    if (className != null) {
      for (IWorkflowService s : SERVICES.getServices(IWorkflowService.class)) {
        if (s.getClass().getName().equals(className)) {
          return s;
        }
      }
    }
    throw new ProcessingException("service \"" + className + "\" not found");
  }

}
