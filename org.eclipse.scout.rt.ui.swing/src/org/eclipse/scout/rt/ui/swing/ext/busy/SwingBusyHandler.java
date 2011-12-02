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
package org.eclipse.scout.rt.ui.swing.ext.busy;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.busy.AbstractBusyHandler;
import org.eclipse.scout.rt.client.busy.IBusyHandler;

/**
 * Swing default implementation of busy handling.
 * <p>
 * Show wait cursor as long as busy.
 * <p>
 * Block application after {@link IBusyHandler#getLongOperationMillis()}
 * 
 * @author imo
 * @since 3.8
 */
public class SwingBusyHandler extends AbstractBusyHandler {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingBusyHandler.class);

  public SwingBusyHandler(IClientSession session) {
    super(session);
  }

  @Override
  protected void runBusy() {
    new SwingBusyJob("Waiting", this).schedule();
  }

}
