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

import static org.junit.Assert.*;

import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.Status;
import org.json.JSONObject;
import org.junit.Test;

public class JsonStatusTest {

  @Test
  public void testToJson() {
    assertNull(JsonStatus.toJson(null));
    Status status = new Status("foo", IStatus.INFO);
    JSONObject json = JsonStatus.toJson(status);
    assertEquals("foo", json.getString("message"));
    assertEquals(IStatus.INFO, json.getInt("severity"));
  }
}
