/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.pop3;

import javax.mail.Message;
import javax.mail.MessagingException;

@FunctionalInterface
public interface IPOP3MessageVisitor {
  /**
   * @return true to continue visiting, false to break up further visiting
   */
  boolean visit(Message m) throws MessagingException;
}
