/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest;

public interface RestApplicationScopes {

  /**
   * Used for rest resources that provided an API for the application itself.
   */
  String API = "api";

  /**
   * Scope for rest resources that provide an API for external services.
   */
  String EXT = "ext";
}
