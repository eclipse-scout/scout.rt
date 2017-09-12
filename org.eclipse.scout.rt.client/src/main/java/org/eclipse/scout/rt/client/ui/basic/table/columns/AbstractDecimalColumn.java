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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.IDecimalColumnExtension;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.decimalfield.IDecimalField;
import org.eclipse.scout.rt.client.ui.valuecontainer.IDecimalValueContainer;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.NumberFormatProvider;

/**
 * Column holding Decimal number
 */
@ClassId("961989bf-d585-40a2-ab9f-b7e545baaac9")
public abstract class AbstractDecimalColumn<NUMBER extends Number> extends AbstractNumberColumn<NUMBER> implements IDecimalColumn<NUMBER> {

  public AbstractDecimalColumn() {
    this(true);
  }

  public AbstractDecimalColumn(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected int getConfiguredHorizontalAlignment() {
    return 1;
  }

  @Override
  protected RoundingMode getConfiguredRoundingMode() {
    return RoundingMode.HALF_UP;
  }

  /*
   * Configuration
   */
  /**
   * Configures the minimum number of fraction digits used to display the value. To use an exact number of fraction
   * digits, the same number as for {@link #getConfiguredMaxFractionDigits()} must be returned.
   * <p>
   * This property only has an effect if no format is specified by {@link #getConfiguredFormat()}.
   * <p>
   * Subclasses can override this method. Default is {@code 2}.
   *
   * @return Minimum number of fraction digits of this column.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(160)
  protected int getConfiguredMinFractionDigits() {
    return 2;
  }

  /**
   * Configures the maximum number of fraction digits used to display the value. To use an exact number of fraction
   * digits, the same number as for {@link #getConfiguredMinFractionDigits()} must be returned.
   * <p>
   * This property only has an effect if no format is specified by {@link #getConfiguredFormat()}.
   * <p>
   * Subclasses can override this method. Default is {@code 2}.
   *
   * @return maximum number of fraction digits of this column.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(170)
  protected int getConfiguredMaxFractionDigits() {
    return 2;
  }

  /**
   * Configures whether the value is a percentage and is displayed with the appropriate sign. A value of 12 is displayed
   * as 12 % (depending on the locale). Use {@link #getConfiguredMultiplier()} to handle the value differently (e.g.
   * display a value of 0.12 as 12 %).
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   *
   * @return {@code true} if the column represents a percentage value, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(180)
  protected boolean getConfiguredPercent() {
    return false;
  }

  /**
   * Configures the multiplier used to display the value. See {@link DecimalFormat#setMultiplier(int)} for more
   * information about multipliers.
   * <p>
   * Subclasses can override this method. Default is {@code 1}.
   *
   * @return The multiplier used to display the value.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(190)
  protected int getConfiguredMultiplier() {
    return 1;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(200)
  protected int getConfiguredFractionDigits() {
    return 2;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setMinFractionDigits(getConfiguredMinFractionDigits());
    setMaxFractionDigits(getConfiguredMaxFractionDigits());
    setPercent(getConfiguredPercent());
    setFractionDigits(getConfiguredFractionDigits());
    setMultiplier(getConfiguredMultiplier());
  }

  /*
   * Runtime
   */
  @Override
  public void setMinFractionDigits(int i) {
    DecimalFormat format = getFormat();
    format.setMinimumFractionDigits(i);
    setFormat(format);
  }

  @Override
  public int getMinFractionDigits() {
    return getFormatInternal().getMinimumFractionDigits();
  }

  @Override
  public void setMaxFractionDigits(int i) {
    DecimalFormat format = getFormat();
    format.setMaximumFractionDigits(i);
    setFormat(format);
  }

  @Override
  public int getMaxFractionDigits() {
    return getFormatInternal().getMaximumFractionDigits();
  }

  @Override
  public void setPercent(boolean b) {
    DecimalFormat percentDF = BEANS.get(NumberFormatProvider.class).getPercentInstance(NlsLocale.get());
    DecimalFormat format = getFormat();
    if (b) {
      format.setPositiveSuffix(percentDF.getPositiveSuffix());
      format.setNegativeSuffix(percentDF.getNegativeSuffix());
    }
    else {
      if (isPercent()) {
        format.setPositiveSuffix("");
        format.setNegativeSuffix("");
      }
    }
    setFormat(format);
  }

  @Override
  public boolean isPercent() {
    NumberFormat percentNF = BEANS.get(NumberFormatProvider.class).getPercentInstance(NlsLocale.get());
    if (percentNF instanceof DecimalFormat) {
      DecimalFormat percentDF = (DecimalFormat) percentNF;
      DecimalFormat internalDF = getFormatInternal();
      return internalDF.getPositiveSuffix().equals(percentDF.getPositiveSuffix()) && internalDF.getNegativeSuffix().equals(percentDF.getNegativeSuffix());
    }
    else {
      throw new NumberFormatException();
    }
  }

  @Override
  public void setFractionDigits(int i) {
    propertySupport.setPropertyInt(IDecimalValueContainer.PROP_PARSING_FRACTION_DIGITS, i);
  }

  @Override
  public int getFractionDigits() {
    return propertySupport.getPropertyInt(IDecimalValueContainer.PROP_PARSING_FRACTION_DIGITS);
  }

  @Override
  public void setMultiplier(int i) {
    DecimalFormat format = getFormat();
    format.setMultiplier(i);
    setFormat(format);
  }

  @Override
  public int getMultiplier() {
    return getFormatInternal().getMultiplier();
  }

  @Override
  protected IFormField prepareEditInternal(ITableRow row) {
    IDecimalField<NUMBER> f = createDefaultEditor();
    mapEditorFieldProperties(f);
    return f;
  }

  @Override
  protected abstract IDecimalField<NUMBER> createDefaultEditor();

  protected void mapEditorFieldProperties(IDecimalField<NUMBER> f) {
    super.mapEditorFieldProperties(f);
    f.setFractionDigits(getFractionDigits());
  }

  protected static class LocalDecimalColumnExtension<NUMBER extends Number, OWNER extends AbstractDecimalColumn<NUMBER>> extends LocalNumberColumnExtension<NUMBER, OWNER> implements IDecimalColumnExtension<NUMBER, OWNER> {

    public LocalDecimalColumnExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IDecimalColumnExtension<NUMBER, ? extends AbstractDecimalColumn<NUMBER>> createLocalExtension() {
    return new LocalDecimalColumnExtension<>(this);
  }
}
