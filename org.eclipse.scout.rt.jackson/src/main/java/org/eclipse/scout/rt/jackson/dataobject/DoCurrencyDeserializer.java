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

import org.eclipse.scout.rt.platform.util.StringUtility;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.jdk.JDKFromStringDeserializer;

/**
 * Custom deserializer for {@link Currency} that specifically allows deserializing currency strings in upper and lower
 * case.
 */
public class DoCurrencyDeserializer extends JDKFromStringDeserializer {

  protected DoCurrencyDeserializer() {
    super(Currency.class, STD_CURRENCY);
  }

  @Override
  public Object _deserialize(String value, DeserializationContext ctxt) throws JacksonException {
    return Currency.getInstance(StringUtility.uppercase(value)); // Expects an uppercase string value
  }
}
