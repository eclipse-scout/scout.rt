/*
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.integerfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.numberfield.AbstractNumberFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.AbstractIntegerField;

public abstract class AbstractIntegerFieldExtension<OWNER extends AbstractIntegerField> extends AbstractNumberFieldExtension<Integer, OWNER> implements IIntegerFieldExtension<OWNER> {

  public AbstractIntegerFieldExtension(OWNER owner) {
    super(owner);
  }
}
