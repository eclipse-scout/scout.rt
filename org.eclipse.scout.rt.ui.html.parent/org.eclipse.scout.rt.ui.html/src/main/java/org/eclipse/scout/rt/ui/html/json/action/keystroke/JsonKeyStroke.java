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
package org.eclipse.scout.rt.ui.html.json.action.keystroke;

import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.action.JsonAction;

public class JsonKeyStroke<T extends IKeyStroke> extends JsonAction<T> {

  public JsonKeyStroke(T model, IJsonSession jsonSession, String id) {
    super(model, jsonSession, id);
  }

  @Override
  public String getObjectType() {
    return "KeyStroke";
  }
}
