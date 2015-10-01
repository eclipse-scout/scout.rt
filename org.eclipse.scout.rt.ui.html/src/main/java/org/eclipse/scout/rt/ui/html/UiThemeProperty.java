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
package org.eclipse.scout.rt.ui.html;

import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;

/**
 * Returns the name of the UI theme which is activated when the application starts. When the default theme is active
 * this property will return null.
 */
public class UiThemeProperty extends AbstractStringConfigProperty {

  @Override
  public String getKey() {
    return "scout.ui.theme";
  }

}
