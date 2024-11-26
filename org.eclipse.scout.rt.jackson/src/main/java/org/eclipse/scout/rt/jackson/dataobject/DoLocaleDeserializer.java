/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject;

import java.io.IOException;
import java.util.Locale;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer.Std;

/**
 * Custom deserializer for {@link Locale} that specifically only handles deserializing "und" into the
 * {@link Locale#ROOT} value. All other values are deserialized using the default implementation.
 * <p>
 * TODO pbz: Remove this class when Jackson is upgraded to 3.0 (issue 1600)
 */
public class DoLocaleDeserializer extends Std {

  private static final long serialVersionUID = 2429460681017940619L;
  public static final String UNDETERMINED = "und"; // from sun.util.locale.LanguageTag.UNDETERMINED

  protected DoLocaleDeserializer() {
    super(Locale.class, STD_LOCALE);
  }

  @Override
  protected Object _deserialize(String value, DeserializationContext ctxt) throws IOException {
    if (UNDETERMINED.equals(value)) {
      return Locale.ROOT;
    }
    return super._deserialize(value, ctxt);
  }
}
