/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.http;

import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.shared.http.DefaultHttpTransportManager;

/**
 * HTTP Client supporting interception of http. Used to trigger and force errors and failures.
 */
@IgnoreBean
public class TestingHttpClient extends DefaultHttpTransportManager {
  public void stop() {
    removeHttpTransport();
  }
}
