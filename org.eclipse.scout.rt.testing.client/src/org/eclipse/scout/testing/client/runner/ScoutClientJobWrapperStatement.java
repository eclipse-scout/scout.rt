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

package org.eclipse.scout.testing.client.runner;

import org.eclipse.scout.rt.client.IClientSession;
import org.junit.runners.model.Statement;

/**
 * Deprecated: use {@link org.eclipse.scout.rt.testing.client.runner.ScoutClientJobWrapperStatement} instead
 * will be removed with the L-Release.
 */
@Deprecated
public class ScoutClientJobWrapperStatement extends org.eclipse.scout.rt.testing.client.runner.ScoutClientJobWrapperStatement {

  public ScoutClientJobWrapperStatement(IClientSession clientSession, Statement statement) {
    super(clientSession, statement);
  }
}
