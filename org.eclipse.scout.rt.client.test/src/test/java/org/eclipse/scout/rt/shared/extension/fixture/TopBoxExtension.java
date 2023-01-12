/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.extension.fixture;

import org.eclipse.scout.rt.client.extension.ui.form.fields.groupbox.AbstractGroupBoxExtension;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.extension.fixture.BasicForm.MainBox.TopBox;

public class TopBoxExtension extends AbstractGroupBoxExtension<BasicForm.MainBox.TopBox> {

  public TopBoxExtension(TopBox owner) {
    super(owner);
  }

  // not a replacement, but a second name field with the same behavior
  @Order(20)
  public class SecondNameField extends BasicForm.MainBox.TopBox.NameField {

    public SecondNameField(BasicForm.MainBox.TopBox container) {
      container.super();
    }
  }
}
