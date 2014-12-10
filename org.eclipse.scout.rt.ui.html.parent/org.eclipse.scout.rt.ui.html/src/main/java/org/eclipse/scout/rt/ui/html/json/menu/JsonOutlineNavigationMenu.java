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
package org.eclipse.scout.rt.ui.html.json.menu;

import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutlineNavigationMenu;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;

public class JsonOutlineNavigationMenu<T extends AbstractOutlineNavigationMenu> extends JsonMenu<T> {

  private static final String PROP_OUTLINE = "outline";

  public JsonOutlineNavigationMenu(T model, IJsonSession jsonSession, String id) {
    super(model, jsonSession, id);
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<T>(PROP_OUTLINE, model, getJsonSession()) {
      @Override
      protected IOutline modelValue() {
        return getModel().getOutline();
      }
    });
  }

}
