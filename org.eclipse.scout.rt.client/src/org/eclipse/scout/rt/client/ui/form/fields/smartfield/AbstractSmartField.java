/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import org.eclipse.scout.commons.annotations.ClassId;

/**
 * import org.eclipse.scout.commons.annotations.ClassId;
 * A content assist field allowing only values out of the lookup row set.
 * 
 * @param <T>
 *          The value type and key type of the lookup rows.
 */
@ClassId("8129f1a3-5f00-4973-b089-c3dbd91d1c9d")
public abstract class AbstractSmartField<T> extends AbstractMixedSmartField<T, T> implements ISmartField<T> {

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
  protected final T execConvertKeyToValue(T key) {
    return super.execConvertKeyToValue(key);
  }

  /**
   * avoid from further overriding
   */
  @Override
  protected final T execConvertValueToKey(T value) {
    return super.execConvertValueToKey(value);
  }
}
