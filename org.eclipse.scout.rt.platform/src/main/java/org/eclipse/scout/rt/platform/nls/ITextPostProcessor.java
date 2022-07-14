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
package org.eclipse.scout.rt.platform.nls;

import java.util.Locale;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.text.NlsKey;

/**
 * Represents a text post processor.<br>
 * Use {@link NlsUtility#postProcessText(String, String, String...)} or one of its overloads to post process a text.
 *
 * @see NlsUtility#postProcessText(String, String, String...)
 * @since 22.0
 */
@ApplicationScoped
public interface ITextPostProcessor {

  /**
   * Applies the post-processing to the text given.
   *
   * @param textLocale
   *     The {@link Locale} of the text given. May be {@code null}.
   * @param textKey
   *     The text key of the text to post-process. May be {@code null}.
   * @param text
   *     The text to post process. May be {@code null}.
   * @param messageArguments
   *     Values of possible placeholders, as used in {@link NlsUtility#bindText}.
   * @return The processed text.
   */
  String apply(Locale textLocale, @NlsKey String textKey, String text, String... messageArguments);
}
