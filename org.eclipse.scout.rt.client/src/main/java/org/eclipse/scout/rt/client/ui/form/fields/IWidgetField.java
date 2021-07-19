/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.form.fields;

import org.eclipse.scout.rt.client.ui.IWidget;

public interface IWidgetField<WIDGET extends IWidget> extends IFormField {

  String PROP_SCROLLABLE = "scrollable";
  String PROP_FIELD_WIDGET = "fieldWidget";

  boolean isScrollable();

  void setScrollable(boolean scrollable);

  WIDGET getFieldWidget();

  void setFieldWidget(WIDGET fieldWidget);
}
