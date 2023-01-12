/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fixture;

import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.platform.Order;

public abstract class AbstractTemplateGroupsBox extends AbstractGroupBox {

  public TopFieldsBox getTopFieldsBox() {
    return getFieldByClass(TopFieldsBox.class);
  }

  public BottomFieldsBox getBottomFieldsBox() {
    return getFieldByClass(BottomFieldsBox.class);
  }

  @Order(10)
  public class TopFieldsBox extends AbstractTemplateFieldsBox {
  }

  @Order(20)
  public class BottomFieldsBox extends AbstractTemplateFieldsBox {
  }
}
