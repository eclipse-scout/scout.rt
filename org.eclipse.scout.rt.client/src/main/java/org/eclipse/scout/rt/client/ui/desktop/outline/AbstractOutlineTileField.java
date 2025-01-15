/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline;

import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("4c21c681-ff91-40ba-a841-406546150b1b")
public abstract class AbstractOutlineTileField extends AbstractFormField implements IOutlineTileField {

  @Override
  protected double getConfiguredGridWeightY() {
    return 1;
  }

  @Override
  public IOutline getOutline() {
    return (IOutline) getProperty(PROP_OUTLINE);
  }

  @Override
  public void setOutline(IOutline outline) {
    setProperty(PROP_OUTLINE, outline);
  }
}
