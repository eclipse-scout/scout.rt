/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.server.commons.servlet.UrlHints;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.json.JSONObject;

/**
 * @since 5.2
 */
@ApplicationScoped
public class InspectorInfo {

  public void put(IUiSession uiSession, JSONObject json, Object model) {
    if (model != null && UrlHints.isInspectorHint(uiSession.currentHttpRequest())) {
      json.put("modelClass", model.getClass().getName());
      if (model instanceof ITypeWithClassId) {
        String classId = ((ITypeWithClassId) model).classId();
        if (ObjectUtility.notEquals(json.get("uuid"), classId)) { // FIXME bsh [js-bookmark] improve this
          json.put("classId", classId);
        }
      }
    }
  }
}
