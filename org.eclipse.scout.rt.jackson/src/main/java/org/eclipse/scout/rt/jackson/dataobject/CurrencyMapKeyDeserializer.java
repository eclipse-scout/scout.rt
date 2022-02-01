/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject;

import java.util.Currency;

import com.fasterxml.jackson.databind.deser.std.StdKeyDeserializer;

/**
 * Custom map key deserializer for {@link Currency} that is based upon the {@link DoCurrencyDeserializer} in order to
 * accept currency strings in upper and also lower case.
 */
public class CurrencyMapKeyDeserializer extends StdKeyDeserializer {

  private static final long serialVersionUID = 0L;

  protected CurrencyMapKeyDeserializer() {
    super(TYPE_CURRENCY, Currency.class, new DoCurrencyDeserializer());
  }
}
