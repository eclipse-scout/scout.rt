/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
