/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.ui.html.IUiSession;

/**
 * @since 8.0
 */
public abstract class AbstractJsonWidget<T extends IWidget> extends AbstractJsonPropertyObserver<T> {

  public AbstractJsonWidget(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "Widget";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<T>(IWidget.PROP_CSS_CLASS, model) {
      @Override
      protected String modelValue() {
        return getModel().getCssClass();
      }
    });
  }
}
