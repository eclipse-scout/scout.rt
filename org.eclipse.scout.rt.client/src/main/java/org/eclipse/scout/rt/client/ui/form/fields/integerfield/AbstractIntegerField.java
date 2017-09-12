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
package org.eclipse.scout.rt.client.ui.form.fields.integerfield;

import java.math.BigDecimal;

import org.eclipse.scout.rt.client.extension.ui.form.fields.integerfield.IIntegerFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("4418100d-db7c-40e7-84b4-29df65534671")
public abstract class AbstractIntegerField extends AbstractNumberField<Integer> implements IIntegerField {
  public AbstractIntegerField() {
    this(true);
  }

  public AbstractIntegerField(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */

  @Override
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(250)
  protected Integer getConfiguredMinValue() {
    return Integer.MIN_VALUE;
  }

  @Override
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(260)
  protected Integer getConfiguredMaxValue() {
    return Integer.MAX_VALUE;
  }

  @Override
  @Order(270)
  @ConfigProperty(ConfigProperty.INTEGER)
  protected int getConfiguredMaxIntegerDigits() {
    return 10;
  }

  @Override
  protected Integer getMinPossibleValue() {
    return Integer.MIN_VALUE;
  }

  @Override
  protected Integer getMaxPossibleValue() {
    return Integer.MAX_VALUE;
  }

  /**
   * uses {@link #parseToBigDecimalInternal(String)} to parse text and returns the result as Integer
   */
  @Override
  protected Integer parseValueInternal(String text) {
    Integer retVal = null;
    BigDecimal parsedVal = parseToBigDecimalInternal(text);
    if (parsedVal != null) {
      retVal = parsedVal.intValueExact();
    }
    return retVal;
  }

  protected static class LocalIntegerFieldExtension<OWNER extends AbstractIntegerField> extends LocalNumberFieldExtension<Integer, OWNER> implements IIntegerFieldExtension<OWNER> {

    public LocalIntegerFieldExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IIntegerFieldExtension<? extends AbstractIntegerField> createLocalExtension() {
    return new LocalIntegerFieldExtension<>(this);
  }

}
