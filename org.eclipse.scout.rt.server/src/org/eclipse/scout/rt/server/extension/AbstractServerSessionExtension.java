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
package org.eclipse.scout.rt.server.extension;

import org.eclipse.scout.rt.server.AbstractServerSession;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.extension.ServerSessionChains.ServerSessionLoadSessionChain;

/**
 *
 */
public class AbstractServerSessionExtension<OWNER extends AbstractServerSession> extends AbstractExtension<OWNER> implements IServerSessionExtension<OWNER> {

  public AbstractServerSessionExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execLoadSession(ServerSessionLoadSessionChain chain) throws ProcessingException {
    chain.execLoadSession();
  }

}
