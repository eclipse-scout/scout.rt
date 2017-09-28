/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.http;

import org.eclipse.scout.rt.platform.ApplicationScoped;

import com.google.api.client.http.HttpTransport;

/**
 * Factory (helper) for {@link IHttpTransportManager} (especially {@link AbstractHttpTransportManager}) classes to
 * create new instances of {@link HttpTransport}.
 */
@FunctionalInterface
@ApplicationScoped
public interface IHttpTransportFactory {

  /**
   * Create a new {@link HttpTransport} for the specific {@link IHttpTransportManager}.
   */
  HttpTransport newHttpTransport(IHttpTransportManager manager);

}
