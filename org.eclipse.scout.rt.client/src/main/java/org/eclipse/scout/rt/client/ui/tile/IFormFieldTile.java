/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.tile;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

public interface IFormFieldTile<T extends IFormField> extends IWidgetTile<T> {
  String PROP_DISPLAY_STYLE = "displayStyle";

  /**
   * The plain style tries to render the form field as it is without adjusting the look or behavior. This gives you an
   * easy possibility to style it as you like.
   */
  String DISPLAY_STYLE_PLAIN = "plain";
  /**
   * This style is designed to be used for tiles on a dashboard.
   */
  String DISPLAY_STYLE_DASHBOARD = "dashboard";

  String getDisplayStyle();
}
