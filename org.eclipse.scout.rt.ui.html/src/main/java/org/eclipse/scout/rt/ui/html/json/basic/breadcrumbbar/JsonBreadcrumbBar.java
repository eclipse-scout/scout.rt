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
package org.eclipse.scout.rt.ui.html.json.basic.breadcrumbbar;

import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.breadcrumbbar.IBreadcrumbBar;
import org.eclipse.scout.rt.client.ui.basic.breadcrumbbar.IBreadcrumbItem;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonWidget;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;

public class JsonBreadcrumbBar<T extends IBreadcrumbBar> extends AbstractJsonWidget<T> {
  public JsonBreadcrumbBar(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "BreadcrumbBar";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<T>(IBreadcrumbBar.PROP_BREADCRUMBS, model, getUiSession()) {
      @Override
      protected List<IBreadcrumbItem> modelValue() {
        return getModel().getBreadcrumbItems();
      }

      @Override
      public String jsonPropertyName() {
        return "breadcrumbItems";
      }
    });
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapters(getModel().getBreadcrumbItems());
  }
}
