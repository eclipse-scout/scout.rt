/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
