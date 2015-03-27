/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.server.runner;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.AbstractServerSession;

/**
 * May be removed when running without osgi.
 */
public class TestServerSession extends AbstractServerSession {
  private static final long serialVersionUID = 1L;
  private static final String BUNDLE_NAME = "org.eclipse.scout.rt.testing.server.test";

  public TestServerSession(boolean autoInitConfig) {
    super(autoInitConfig);
  }

  @Override
  public void loadSession() throws ProcessingException {

    super.loadSession(Platform.getBundle(BUNDLE_NAME));
  }

}
