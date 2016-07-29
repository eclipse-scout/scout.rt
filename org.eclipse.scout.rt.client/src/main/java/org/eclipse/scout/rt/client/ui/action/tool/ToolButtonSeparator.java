/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.action.tool;

import org.eclipse.scout.rt.client.ui.action.menu.MenuSeparator;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * @deprecated use {@link MenuSeparator} instead, will be removed in Scout 6.1
 */
@Deprecated
@SuppressWarnings("deprecation")
@ClassId("3fcbae56-446a-4a03-b668-2c510e91d2c5")
public class ToolButtonSeparator extends AbstractToolButton {

  public ToolButtonSeparator() {
    super();
  }

  @Override
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(10)
  protected boolean getConfiguredSeparator() {
    return true;
  }

  @Override
  @ConfigOperation
  @Order(10)
  protected void execAction() {
    // tool button separator has no action
  }
}
