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
package org.eclipse.scout.rt.client.mobile.ui.form.fields.button;

import org.eclipse.scout.rt.client.mobile.Icons;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * @since 3.9.0
 */
@ClassId("c4faa795-efda-47d3-9a89-dc249edd002a")
public abstract class AbstractBackButton extends AbstractButton implements IMobileButton {

  @Override
  protected String getConfiguredLabel() {
    return null;
  }

  @Override
  protected String getConfiguredIconId() {
    return Icons.BackAction;
  }

  @Override
  protected String getConfiguredTooltipText() {
    return null;
  }

  @Override
  protected int getConfiguredSystemType() {
    return SYSTEM_TYPE_BACK;
  }

  @Override
  protected void execClickAction() {
    getForm().doOk();
  }

}
