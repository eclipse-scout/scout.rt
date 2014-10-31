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
package org.eclipse.scout.rt.client.ui.form.fields.sequencebox;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

public class AbstractSequenceBox5 extends AbstractSequenceBox {

  @Override
  protected void initConfig() {
    super.initConfig();

    modifyFieldLabels();
  }

  private void modifyFieldLabels() {
    for (IFormField field : getFields()) {
      field.setLabelPosition(IFormField.LABEL_POSITION_ON_FIELD);
      field.setLabelVisible(false);
    }
  }

  @Override
  protected final String execCreateLabelSuffix() {
    return "";
  }

}
