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

import java.util.Date;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.ValueFieldChains.ValueFieldParseValueChain;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;

public abstract class AbstractDateFieldExtension<OWNER extends AbstractDateField> extends AbstractValueFieldExtension<Date, OWNER> implements IDateFieldExtension<OWNER> {

  public AbstractDateFieldExtension(OWNER owner) {
    super(owner);
  }

  /**
   * parsing is not longer supported on model. Client parses date and sets value;
   */
  @Override
  public final Date execParseValue(ValueFieldParseValueChain<Date> chain, String text) {
    return null;
  }
}
