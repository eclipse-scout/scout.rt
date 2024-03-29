/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.testing.signature;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.dataobject.id.TypedId;
import org.eclipse.scout.rt.platform.resource.BinaryResource;

public class ScoutDataObjectSignatureTestCustomizer implements IDataObjectSignatureTestCustomizer {

  private static final Set<Class<?>> SUPPORTED_TYPES = new HashSet<>(Arrays.asList(
      // Java basic types
      Long.class,
      Character.class,
      String.class,
      Date.class,
      Boolean.class,
      Double.class,
      Float.class,
      Integer.class,
      BigDecimal.class,
      Locale.class,
      Currency.class,
      URI.class,
      byte[].class,

      // Java collections
      Map.class,
      Set.class,
      List.class, // required for DoValue<Map<String, List<LoremDo>>>, not desired for DoValue<List<LoremDo>> because DoList should be used instead

      // Scout basic types
      BinaryResource.class,
      TypedId.class));

  @Override
  public Set<Class<?>> supportedTypes() {
    return SUPPORTED_TYPES;
  }
}
