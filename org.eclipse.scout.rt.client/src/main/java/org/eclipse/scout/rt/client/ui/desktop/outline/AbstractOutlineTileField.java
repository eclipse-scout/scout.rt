/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
