/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json;

import org.eclipse.scout.rt.client.ui.form.fields.ValidationFailedStatus;
import org.json.JSONObject;

public class JsonValidationFailedStatus extends JsonStatus {

  public JsonValidationFailedStatus(ValidationFailedStatus status) {
    super(status);
  }

  @Override
  public String getObjectType() {
    return "ValidationFailedStatus";
  }

  /**
   * Set the 'deletable' flag to false. The UI is not allowed to remove validation errors from the server.
   *
   * @return
   */
  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put("deletable", false);
    return json;
  }
}
