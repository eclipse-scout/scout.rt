/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.button;

import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("bd045e80-5577-459c-a212-3d5c655e304a")
public abstract class AbstractLinkButton extends AbstractButton {

  public AbstractLinkButton() {
    this(true);
  }

  public AbstractLinkButton(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected int getConfiguredDisplayStyle() {
    return DISPLAY_STYLE_LINK;
  }
}
