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
package org.eclipse.scout.rt.ui.html.json.fixtures;

import org.eclipse.scout.rt.ui.html.json.AbstractJsonAdapter;

public class JsonAdapterMock extends AbstractJsonAdapter<Object> {
  public JsonAdapterMock() {
    super(new Object(), new UiSessionMock(), null, null);
  }

  @Override
  public String getObjectType() {
    return null;
  }
}
