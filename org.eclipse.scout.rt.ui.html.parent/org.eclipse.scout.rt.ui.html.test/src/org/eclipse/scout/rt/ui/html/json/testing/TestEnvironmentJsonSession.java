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
package org.eclipse.scout.rt.ui.html.json.testing;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonSession;

public class TestEnvironmentJsonSession extends AbstractJsonSession {

  @Override
  protected IClientSession createClientSession() {
    return TestEnvironmentClientSession.get();
  }

  @Override
  protected Subject currentSubject() {
    Subject subject = new Subject();
    subject.getPrincipals().add(new SimplePrincipal("tester"));
    return subject;
  }

}
