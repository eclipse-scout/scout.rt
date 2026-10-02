/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject;

import java.util.Currency;

import tools.jackson.databind.deser.jdk.JDKKeyDeserializer;

/**
 * Custom map key deserializer for {@link Currency} that is based upon the {@link DoCurrencyDeserializer} in order to
 * accept currency strings in upper and also lower case.
 */
public class CurrencyMapKeyDeserializer extends JDKKeyDeserializer {

  protected CurrencyMapKeyDeserializer() {
    super(TYPE_CURRENCY, Currency.class, new DoCurrencyDeserializer());
  }
}
