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
package org.eclipse.scout.rt.server;

/**
 * A test server session with shared context variables. <br>
 * Has to be located in package <code>org.eclipse.scout.rt.server</code>, because ServerSessionRegistryService requires
 * .* it
 */
public class TestServerSession extends AbstractServerSession {
  private static final long serialVersionUID = 782294551137415747L;

  public TestServerSession() {
    super(true);
  }

  @Override
  protected void execLoadSession() {
    setSharedContextVariable("test", String.class, "testval");
  }
}
