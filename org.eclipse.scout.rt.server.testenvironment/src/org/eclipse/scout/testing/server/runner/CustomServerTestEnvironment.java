/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.testing.server.runner;

import org.eclipse.scout.rt.server.testenvironment.TestEnvironmentServerSession;
import org.eclipse.scout.rt.testing.server.runner.IServerTestEnvironment;
import org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner;

/**
 * Injection of Test Environment settings
 * 
 * @author amo
 */
public class CustomServerTestEnvironment implements IServerTestEnvironment {

  @Override
  public void setupGlobalEnvironment() {
    ScoutServerTestRunner.setDefaultServerSessionClass(TestEnvironmentServerSession.class);
  }

  @Override
  public void setupInstanceEnvironment() {
  }

}
