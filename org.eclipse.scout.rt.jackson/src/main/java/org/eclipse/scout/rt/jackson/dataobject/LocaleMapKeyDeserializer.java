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

import java.util.Locale;

import com.fasterxml.jackson.databind.deser.std.StdKeyDeserializer;

/**
 * Custom map key deserializer for {@link Locale} that is based upon the {@link DoLocaleDeserializer} in order to handle
 * the root locale correctly. TODO [23.0] pbz: Remove this class when Jackson is upgraded to 3.0 (issue 1600)
 */
public class LocaleMapKeyDeserializer extends StdKeyDeserializer {
  private static final long serialVersionUID = 1L;

  protected LocaleMapKeyDeserializer() {
    super(TYPE_LOCALE, Locale.class, new DoLocaleDeserializer());
  }
}
