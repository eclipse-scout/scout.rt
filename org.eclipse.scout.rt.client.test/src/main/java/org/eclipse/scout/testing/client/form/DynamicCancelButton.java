/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.testing.client.form;

import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * Dynamic field to build an ad-hoc application for testing
 */
@ClassId("1d8c91fa-2cf2-46be-bfa9-3d46fe2eb907")
public class DynamicCancelButton extends AbstractCancelButton {

  public DynamicCancelButton() {
    super();
    setProperty("id", "cancel");
  }

  @Override
  public String getFieldId() {
    return (String) getProperty("id");
  }

}
