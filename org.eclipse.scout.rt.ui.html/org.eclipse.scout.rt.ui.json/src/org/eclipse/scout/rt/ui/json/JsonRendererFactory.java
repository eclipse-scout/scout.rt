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
package org.eclipse.scout.rt.ui.json;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.ui.json.desktop.JsonDesktop;
import org.eclipse.scout.rt.ui.json.desktop.JsonDesktopTree;
import org.eclipse.scout.rt.ui.json.desktop.JsonViewButton;
import org.eclipse.scout.rt.ui.json.form.JsonForm;
import org.eclipse.scout.rt.ui.json.form.fields.IJsonFormField;
import org.eclipse.scout.rt.ui.json.form.fields.JsonFormField;
import org.eclipse.scout.rt.ui.json.form.fields.groupbox.JsonGroupBox;
import org.eclipse.scout.rt.ui.json.form.fields.stringfield.JsonStringField;
import org.eclipse.scout.rt.ui.json.menu.JsonMenu;
import org.eclipse.scout.rt.ui.json.table.JsonTable;

public class JsonRendererFactory {

  private static JsonRendererFactory m_instance;

  public static JsonRendererFactory get() {
    return m_instance;
  }

  public static void init(JsonRendererFactory rendererFactory) {
    if (m_instance != null) {
      throw new IllegalArgumentException("JsonRendererFactory is already initialized.");
    }
    m_instance = rendererFactory;
  }

  public static void init() {
    if (m_instance == null) {
      m_instance = new JsonRendererFactory();
    }
  }

  public JsonClientSession createJsonClientSession(IClientSession model, IJsonSession session, String id) {
    return new JsonClientSession(model, session, id);
  }

  public JsonDesktop createJsonDesktop(IDesktop model, IJsonSession session) {
    JsonDesktop renderer = new JsonDesktop(model, session);
    renderer.init();

    return renderer;
  }

  public JsonTable createJsonTable(ITable model, IJsonSession session) {
    JsonTable renderer = new JsonTable(model, session);
    renderer.init();

    return renderer;
  }

  public JsonDesktopTree createJsonDesktopTree(IOutline model, IJsonSession session) {
    JsonDesktopTree renderer = new JsonDesktopTree(model, session);
    renderer.init();

    return renderer;
  }

  public JsonViewButton createJsonViewButton(IViewButton model, IJsonSession session) {
    JsonViewButton renderer = new JsonViewButton(model, session);
    renderer.init();

    return renderer;
  }

  public JsonForm createJsonForm(IForm model, IJsonSession session) {
    JsonForm renderer = new JsonForm(model, session);
    renderer.init();

    return renderer;
  }

  public JsonMenu createJsonMenu(IMenu model, IJsonSession session) {
    JsonMenu renderer = new JsonMenu(model, session);
    renderer.init();

    return renderer;
  }

  @SuppressWarnings("unchecked")
  public <T extends IJsonFormField> T createJsonFormField(IFormField model, IJsonSession session) {
    T renderer = null;
    if (model instanceof IStringField) {
      renderer = (T) new JsonStringField((IStringField) model, session);
    }
    else if (model instanceof IGroupBox) {
      renderer = (T) new JsonGroupBox((IGroupBox) model, session);
    }
    else {
      renderer = (T) new JsonFormField(model, session);
    }
    renderer.init();

    return renderer;
  }
}
