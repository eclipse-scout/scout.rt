/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
   * filter should keep this header untouched.
   */
  String filter(String name, String value);
}
