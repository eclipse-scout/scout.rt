/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
