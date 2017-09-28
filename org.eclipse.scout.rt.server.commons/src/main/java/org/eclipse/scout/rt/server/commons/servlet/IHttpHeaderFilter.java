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
package org.eclipse.scout.rt.server.commons.servlet;

/**
 * Interface for {@link HttpProxy} to support request/response header filtering.
 */
@FunctionalInterface
public interface IHttpHeaderFilter {

  /**
   * Called to filter the particular header.
   *
   * @return New filtered value for the header or <code>null</code> to remove the header. Just return the same value if
   *         filter should keep this header untouched.
   */
  String filter(String name, String value);

}
