/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.longfield;

import java.math.BigDecimal;

import org.eclipse.scout.rt.client.extension.ui.form.fields.longfield.ILongFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("cfc961a1-195f-491d-94c5-762f9d86efee")
public abstract class AbstractLongField extends AbstractNumberField<Long> implements ILongField {
  public AbstractLongField() {
    this(true);
  }

  public AbstractLongField(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */

  @Override
  @ConfigProperty(ConfigProperty.LONG)
  @Order(250)
  protected Long getConfiguredMinValue() {
    return Long.MIN_VALUE;
  }

  @Override
  @ConfigProperty(ConfigProperty.LONG)
  @Order(260)
  protected Long getConfiguredMaxValue() {
    return Long.MAX_VALUE;
  }

  @Override
  @Order(270)
  @ConfigProperty(ConfigProperty.INTEGER)
  protected int getConfiguredMaxIntegerDigits() {
    return 19;
  }

  @Override
  protected Long getMinPossibleValue() {
    return Long.MIN_VALUE;
  }

  @Override
  protected Long getMaxPossibleValue() {
    return Long.MAX_VALUE;
  }

  /**
   * uses {@link #parseToBigDecimalInternal(String)} to parse text and returns the result as Long
   */
  @Override
  protected Long parseValueInternal(String text) {
    Long retVal = null;
    BigDecimal parsedVal = parseToBigDecimalInternal(text);
    if (parsedVal != null) {
      retVal = parsedVal.longValueExact();
    }
    return retVal;
  }

  protected static class LocalLongFieldExtension<OWNER extends AbstractLongField> extends LocalNumberFieldExtension<Long, OWNER> implements ILongFieldExtension<OWNER> {

    public LocalLongFieldExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected ILongFieldExtension<? extends AbstractLongField> createLocalExtension() {
    return new LocalLongFieldExtension<>(this);
  }

}
