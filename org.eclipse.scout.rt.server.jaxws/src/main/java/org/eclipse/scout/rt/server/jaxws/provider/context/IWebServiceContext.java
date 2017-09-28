/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jaxws.provider.context;

import javax.xml.ws.WebServiceContext;

/**
 * Interface to hold information about an ongoing webservice request.
 *
 * @since 5.2
 */
public interface IWebServiceContext {

  /**
   * The JAX-WS {@link WebServiceContext} which is currently associated with the current thread.
   */
  ThreadLocal<WebServiceContext> CURRENT = new ThreadLocal<>();
}
