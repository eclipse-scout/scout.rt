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

import java.util.List;

import org.eclipse.scout.rt.client.IClientSession;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * Deprecated: use {@link org.eclipse.scout.rt.testing.client.runner.RunAftersInSeparateScoutClientSession} instead
 * will be removed with the L-Release.
 */
@Deprecated
public class RunAftersInSeparateScoutClientSession extends org.eclipse.scout.rt.testing.client.runner.RunAftersInSeparateScoutClientSession {

  public RunAftersInSeparateScoutClientSession(IClientSession clientSession, Statement statement, List<FrameworkMethod> afters, Object target) {
    super(clientSession, statement, afters, target);
  }
}
