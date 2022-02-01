/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("TestCurrency")
public class TestCurrencyDo extends DoEntity {

  public DoValue<Currency> currency() {
    return doValue("currency");
  }

  public DoList<Currency> currencies() {
    return doList("currencies");
  }

  public DoValue<Map<Currency, String>> currencyMap() {
    return doValue("currencyMap");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestCurrencyDo withCurrency(Currency currency) {
    currency().set(currency);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Currency getCurrency() {
    return currency().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCurrencyDo withCurrencies(Collection<? extends Currency> currencies) {
    currencies().updateAll(currencies);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCurrencyDo withCurrencies(Currency... currencies) {
    currencies().updateAll(currencies);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<Currency> getCurrencies() {
    return currencies().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestCurrencyDo withCurrencyMap(Map<Currency, String> currencyMap) {
    currencyMap().set(currencyMap);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<Currency, String> getCurrencyMap() {
    return currencyMap().get();
  }
}
