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
package org.eclipse.scout.rt.server.services.common.imap;

import javax.mail.Message;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.service.IService;

/**
 * This service is normally registered as a scout server service extension, so
 * it exists per session
 */
public interface IIMAPService extends IService {

  Message[] getUnreadMessages() throws ProcessingException;

  void deleteMessages(Message... toDelete) throws ProcessingException;

  void deleteAllMessages() throws ProcessingException;

}
