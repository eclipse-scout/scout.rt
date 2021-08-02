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

import java.util.Collection;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBeanManager;

public final class NlsUtility {

  private static final Pattern MESSAGE_ARGUMENT_PATTERN = Pattern.compile("\\{([0-9]+)}");

  private NlsUtility() {
  }

  /**
   * @param text
   *          nls text key
   * @param messageArguments
   *          the translation of the text might contain variables {0},{1},{2},... Examples: getText("MissingFile1");
   *          with translation: MissingFile1=Das File konnte nicht gefunden werden getText("MissingFile2",fileName);
   *          with translation: MissingFile2=Das File {0} konnte nicht gefunden werden.
   *          getText("MissingFile3",fileName,dir); with translation: MissingFile3=Das File {0} im Ordner {1} konnte
   *          nicht gefunden werden
   */
  public static String bindText(String text, String... messageArguments) {
    if (text == null) {
      return text;
    }

    // check potential for message arguments
    if (messageArguments == null || messageArguments.length <= 0) {
      return text;
    }

    Matcher m = MESSAGE_ARGUMENT_PATTERN.matcher(text);
    StringBuilder b = new StringBuilder();
    int start = 0;
    while (m.find(start)) {
      b.append(text, start, m.start());
      int index = Integer.parseInt(m.group(1));
      if (index < messageArguments.length) {
        if (messageArguments[index] != null) {
          b.append(messageArguments[index]);
        }
      }
      else {
        b.append("{").append(index).append("}");
      }
      // next
      start = m.end();
    }
    b.append(text.substring(start));
    return b.toString();
  }

  /**
   * Applies all {@link ITextPostProcessor text post processors} registered in the {@link IBeanManager} to the text
   * given. It is assumed that the given text uses the {@link NlsLocale}.
   *
   * @param text
   *          The text to post-process. May be {@code null}.
   * @return The text with all post-processing applied.
   * @see #postProcessText(Locale, String)
   * @see #postProcessText(Locale, String, Collection)
   */
  public static String postProcessText(String text) {
    return postProcessText(NlsLocale.get(), text);
  }

  /**
   * Applies the {@link ITextPostProcessor text post processors} given to the text given. It is assumed that the given
   * text uses the {@link NlsLocale}.
   *
   * @param text
   *          The text to post-process. May be {@code null}.
   * @param postProcessors
   *          The post processors to execute. May be {@code null}.
   * @return The text with all post-processing applied.
   * @see #postProcessText(Locale, String)
   * @see #postProcessText(Locale, String, Collection)
   */
  public static String postProcessText(String text, Collection<? extends ITextPostProcessor> postProcessors) {
    return postProcessText(NlsLocale.get(), text, postProcessors);
  }

  /**
   * Applies all {@link ITextPostProcessor text post processors} registered in the {@link IBeanManager} to the text
   * given.
   *
   * @param textLocale
   *          The locale of the text given. May be {@code null}.
   * @param text
   *          The text to post-process. May be {@code null}.
   * @return The text with all post-processing applied.
   * @see #postProcessText(String)
   * @see #postProcessText(Locale, String, Collection)
   */
  public static String postProcessText(Locale textLocale, String text) {
    return postProcessText(textLocale, text, BEANS.all(ITextPostProcessor.class));
  }

  /**
   * Applies the {@link ITextPostProcessor text post processors} given to the text given.
   *
   * @param textLocale
   *          The locale of the text given. May be {@code null}.
   * @param text
   *          The text to post-process. May be {@code null}.
   * @param postProcessors
   *          The post processors to execute. May be {@code null}.
   * @return The text with all post-processing applied.
   * @see #postProcessText(Locale, String)
   * @see #postProcessText(String)
   */
  public static String postProcessText(Locale textLocale, String text, Collection<? extends ITextPostProcessor> postProcessors) {
    if (text == null || postProcessors == null || postProcessors.isEmpty()) {
      return text;
    }

    String result = text;
    for (ITextPostProcessor postProcessor : postProcessors) {
      if (postProcessor == null) {
        continue;
      }
      result = postProcessor.apply(textLocale, result);
    }
    return result;
  }
}
