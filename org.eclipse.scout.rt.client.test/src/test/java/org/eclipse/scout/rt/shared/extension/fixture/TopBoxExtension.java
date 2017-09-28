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
