/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.session;

import org.eclipse.scout.rt.server.session.AbstractServerSession;

public class TestServerSession extends AbstractServerSession {
  private static final long serialVersionUID = 782294551137415747L;

  public TestServerSession() {
    super(true);
  }

  @Override
  protected void execLoadSession() {
  }
}
