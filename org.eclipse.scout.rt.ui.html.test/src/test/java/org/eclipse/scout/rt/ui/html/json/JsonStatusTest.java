/*
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
