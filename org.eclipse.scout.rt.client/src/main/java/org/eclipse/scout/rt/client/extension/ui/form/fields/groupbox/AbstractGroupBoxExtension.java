/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.groupbox;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractCompositeFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;

public abstract class AbstractGroupBoxExtension<OWNER extends AbstractGroupBox> extends AbstractCompositeFieldExtension<OWNER> implements IGroupBoxExtension<OWNER> {

  /**
   * @param owner
   */
  public AbstractGroupBoxExtension(OWNER owner) {
    super(owner);
  }
}
