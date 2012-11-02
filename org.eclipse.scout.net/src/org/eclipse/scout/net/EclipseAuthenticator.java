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
package org.eclipse.scout.net;

import java.net.PasswordAuthentication;

import org.eclipse.scout.net.internal.InternalEclipseAuthenticator;

/**
 * java.net Authenticator supporting runtime add/remove of local authenticators.
 * When no local authenticator is available the
 * org.eclipse.core.net.authenticator extension is used as default.
 * 
 * @deprecated this class is only used until
 *             https://bugs.eclipse.org/bugs/show_bug.cgi?id=299756 and
 *             https://bugs.eclipse.org/bugs/show_bug.cgi?id=257443 are solved.
 */
@Deprecated
public final class EclipseAuthenticator extends InternalEclipseAuthenticator {

  @Override
  protected PasswordAuthentication getPasswordAuthentication() {
    return super.getPasswordAuthentication();
  }
}
