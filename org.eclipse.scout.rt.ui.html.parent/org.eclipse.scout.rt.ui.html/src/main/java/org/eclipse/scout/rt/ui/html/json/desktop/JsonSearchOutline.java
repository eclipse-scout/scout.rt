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
package org.eclipse.scout.rt.ui.html.json.desktop;

import org.eclipse.scout.rt.client.ui.desktop.outline.ISearchOutline;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonProperty;

public class JsonSearchOutline<T extends ISearchOutline> extends JsonOutline<T> {

  public JsonSearchOutline(T model, IJsonSession jsonSession, String id) {
    super(model, jsonSession, id);
  }

  @Override
  public String getObjectType() {
    return "SearchOutline";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);

    putJsonProperty(new JsonProperty<T>(ISearchOutline.PROP_SEARCH_QUERY, model) {
      @Override
      protected String modelValue() {
        return getModel().getSearchQuery();
      }
    });
    putJsonProperty(new JsonProperty<T>(ISearchOutline.PROP_SEARCH_STATUS, model) {
      @Override
      protected String modelValue() {
        return getModel().getSearchStatus();
      }
    });
  }

}
