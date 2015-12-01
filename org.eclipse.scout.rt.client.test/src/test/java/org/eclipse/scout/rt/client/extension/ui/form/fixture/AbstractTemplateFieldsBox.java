/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.form.fixture;

import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;

public abstract class AbstractTemplateFieldsBox extends AbstractGroupBox {

  public TopStringField getTopStringField() {
    return getFieldByClass(TopStringField.class);
  }

  public BottomStringField getBottomStringField() {
    return getFieldByClass(BottomStringField.class);
  }

  @Order(10)
  public class TopStringField extends AbstractStringField {
  }

  @Order(20)
  public class BottomStringField extends AbstractStringField {
  }
}
