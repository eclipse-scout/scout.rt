/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.datefield;

import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractUTCDateField;

public abstract class AbstractUTCDateFieldExtension<OWNER extends AbstractUTCDateField> extends AbstractDateFieldExtension<OWNER> implements IUTCDateFieldExtension<OWNER> {

  public AbstractUTCDateFieldExtension(OWNER owner) {
    super(owner);
  }
}
