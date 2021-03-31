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

import java.util.Locale;

import com.fasterxml.jackson.databind.deser.std.StdKeyDeserializer;

/**
 * Custom map key deserializer for {@link Locale} that is based upon the {@link DoLocaleDeserializer} in order to handle
 * the root locale correctly.
 * TODO [22.0] pbz: Remove this class when Jackson is upgraded to 3.0 (issue 1600)
 */
public class LocaleMapKeyDeserializer extends StdKeyDeserializer {

  protected LocaleMapKeyDeserializer() {
    super(TYPE_LOCALE, Locale.class, new DoLocaleDeserializer());
  }
}
