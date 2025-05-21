/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.text;

import java.util.Locale;
import java.util.UUID;

import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.text.ScoutTexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Replace
public class TestScoutTexts extends ScoutTexts {

  private static final Logger LOG = LoggerFactory.getLogger(TestScoutTexts.class);

  @Override
  protected String getTextInternal(Locale locale, String key, String fallback, String... messageArguments) {
    String fallbackSuffix = UUID.randomUUID().toString();
    String textInternal = super.getTextInternal(locale, key, fallback + fallbackSuffix, messageArguments);
    if (textInternal.endsWith(fallbackSuffix)) {
      LOG.error("Text not found for key {}; fallback will be used", key);
      textInternal = textInternal.substring(0, textInternal.length() - fallbackSuffix.length());
    }
    return textInternal;
  }
}
