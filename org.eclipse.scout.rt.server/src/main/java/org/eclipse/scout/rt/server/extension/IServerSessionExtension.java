/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import org.eclipse.scout.rt.server.extension.ServerSessionChains.ServerSessionLoadSessionChain;
import org.eclipse.scout.rt.shared.extension.IExtension;

public interface IServerSessionExtension<OWNER extends AbstractServerSession> extends IExtension<OWNER> {

  void execLoadSession(ServerSessionLoadSessionChain chain);

}
