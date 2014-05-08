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
package org.eclipse.scout.rt.ui.json;

/**
 * Runtime exception used to handle JSON errors. Typically this exception is thrown by code that must try/catch the
 * checked <code>org.json.JSONException</code>.
 */
public class JsonException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public JsonException(String message) {
    super(message);
  }

  public JsonException(Throwable cause) {
    super(cause);
  }

  public JsonException(String message, Throwable cause) {
    super(message, cause);
  }
}
