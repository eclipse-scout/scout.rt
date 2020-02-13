/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
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
