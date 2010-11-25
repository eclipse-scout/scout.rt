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
package org.eclipse.scout.rt.server.services.common.jms;

import javax.jms.Message;

import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.service.IService;

/**
 * Generic interface that needs to be implemented by any service that should be
 * called via JMS. Inherits from IService to make sure the service can be
 * retrieved from Service Registry.
 */
public interface IJmsCallback extends IService {

  /**
   * This method is called inside the server session context
   */
  @ConfigOperation
  void execOnMessage(Message msg, Object value) throws ProcessingException;
}
