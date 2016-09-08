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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield.ISmartFieldExtension;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * import org.eclipse.scout.rt.platform.classid.ClassId; A content assist field allowing only values out of the lookup
 * row set.
 *
 * @param <VALUE>
 *          The value type and key type of the lookup rows.
 */
@ClassId("8129f1a3-5f00-4973-b089-c3dbd91d1c9d")
public abstract class AbstractSmartField<VALUE> extends AbstractMixedSmartField<VALUE, VALUE> implements ISmartField<VALUE> {

  public AbstractSmartField() {
    this(true);
  }

  public AbstractSmartField(boolean callInitializer) {
    super(callInitializer);
  }

  /**
   * avoid from further overriding
   */
  @Override
  @SuppressWarnings("squid:S1185") // method is final
  protected final VALUE execConvertKeyToValue(VALUE key) {
    return super.execConvertKeyToValue(key);
  }

  /**
   * avoid from further overriding
   */
  @Override
  @SuppressWarnings("squid:S1185") // method is final
  protected final VALUE execConvertValueToKey(VALUE value) {
    return super.execConvertValueToKey(value);
  }

  protected static class LocalSmartFieldExtension<VALUE, OWNER extends AbstractSmartField<VALUE>> extends LocalMixedSmartFieldExtension<VALUE, VALUE, OWNER> implements ISmartFieldExtension<VALUE, OWNER> {

    public LocalSmartFieldExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected ISmartFieldExtension<VALUE, ? extends AbstractSmartField<VALUE>> createLocalExtension() {
    return new LocalSmartFieldExtension<VALUE, AbstractSmartField<VALUE>>(this);
  }
}
