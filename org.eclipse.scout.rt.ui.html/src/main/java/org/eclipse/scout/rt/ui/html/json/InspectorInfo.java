/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
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
        json.put("classId", ((ITypeWithClassId) model).classId());
      }
    }
  }
}
