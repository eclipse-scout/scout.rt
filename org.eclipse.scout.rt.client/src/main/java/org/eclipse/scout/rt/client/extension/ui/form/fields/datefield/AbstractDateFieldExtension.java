/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
