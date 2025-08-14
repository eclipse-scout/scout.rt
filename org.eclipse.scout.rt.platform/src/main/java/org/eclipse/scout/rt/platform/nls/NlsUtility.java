/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.nls;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.text.NlsKey;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

public final class NlsUtility {

  private static final Pattern MESSAGE_ARGUMENT_PATTERN = Pattern.compile("\\{([0-9]+)}");

  private NlsUtility() {
  }

  /**
   * @param text
   *     nls text key
   * @param messageArguments
   *     the translation of the text might contain variables {0},{1},{2},... Examples: getText("MissingFile1");
   *     with translation: MissingFile1=Das File konnte nicht gefunden werden getText("MissingFile2",fileName);
   *     with translation: MissingFile2=Das File {0} konnte nicht gefunden werden.
   *     getText("MissingFile3",fileName,dir); with translation: MissingFile3=Das File {0} im Ordner {1} konnte
   *     nicht gefunden werden
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
   * @param textKey
   *     The text key of the text to post-process. May be {@code null}.
   * @param text
   *     The text to post-process. May be {@code null}.
   * @param messageArguments
   *     Values of possible placeholders, as used in {@link #bindText}.
   * @return The text with all post-processing applied.
   * @see #postProcessText(Locale, String, String, Collection, String...)
   */
  public static String postProcessText(@NlsKey String textKey, String text, String... messageArguments) {
    return postProcessText(NlsLocale.get(), textKey, text, BEANS.all(ITextPostProcessor.class), messageArguments);
  }

  /**
   * Applies all {@link ITextPostProcessor text post processors} registered in the {@link IBeanManager} to the text
   * given.
   *
   * @param textLocale
   *     The locale of the text given. May be {@code null}.
   * @param textKey
   *     The text key of the text to post-process. May be {@code null}.
   * @param text
   *     The text to post-process. May be {@code null}.
   * @return The text with all post-processing applied.
   * @see #postProcessText(Locale, String, String, Collection, String...)
   */
  public static String postProcessText(Locale textLocale, @NlsKey String textKey, String text, String... messageArguments) {
    return postProcessText(textLocale, textKey, text, BEANS.all(ITextPostProcessor.class), messageArguments);
  }

  /**
   * Applies given {@link ITextPostProcessor text post processors} to the text given. It is assumed that the given text
   * uses the {@link NlsLocale}.
   *
   * @param textKey
   *     The text key of the text to post-process. May be {@code null}.
   * @param text
   *     The text to post-process. May be {@code null}.
   * @param postProcessors
   *     The post processors to execute. May be {@code null}.
   * @param messageArguments
   *     Values of possible placeholders, as used in {@link #bindText}.
   * @return The text with all post-processing applied.
   * @see #postProcessText(Locale, String, String, Collection, String...)
   */
  public static String postProcessText(@NlsKey String textKey, String text, Collection<? extends ITextPostProcessor> postProcessors, String... messageArguments) {
    return postProcessText(NlsLocale.get(), textKey, text, postProcessors, messageArguments);
  }

  /**
   * Applies the {@link ITextPostProcessor text post processors} given to the text given.
   *
   * @param textLocale
   *     The locale of the text given. May be {@code null}.
   * @param textKey
   *     The text key of the text to post-process. May be {@code null}.
   * @param text
   *     The text to post-process. May be {@code null}.
   * @param postProcessors
   *     The post processors to execute. May be {@code null}.
   * @param messageArguments
   *     Values of possible placeholders, as used in {@link #bindText}.
   * @return The text with all post-processing applied.
   */
  public static String postProcessText(Locale textLocale, @NlsKey String textKey, String text, Collection<? extends ITextPostProcessor> postProcessors, String... messageArguments) {
    if (text == null && textKey == null || CollectionUtility.isEmpty(postProcessors)) {
      return text;
    }

    String result = text;
    for (ITextPostProcessor postProcessor : postProcessors) {
      if (postProcessor == null) {
        continue;
      }
      result = postProcessor.apply(textLocale, textKey, result, messageArguments);
    }
    return result;
  }

  /**
   * Merges all texts from {@code fromTexts} into {@code targetTexts} and returns the result as a new map. The key of each map is an
   * IETF BCP 47 language tag string (e.g. {@code "en-US"}) and the value is a translated text (e.g. {@code "Hello World"}).
   * <p>
   * Language tags that have identical values to entries with shorter tags are automatically removed, as the text is considered
   * inherited from the more general tag. Examples:
   * <ul>
   * <li><tt>{de_DE=groß}</tt> ∪ <tt>{de=groß}</tt> → <tt>{de=groß}</tt>
   * <li><tt>{de_CH=gross}</tt> ∪ <tt>{de=groß}</tt> → <tt>{de=groß, de_CH=gross}</tt>
   * <li><tt>{de=ok}</tt> ∪ <tt>{en=ok}</tt> → <tt>{de=ok, en=ok}</tt>
   * </ul>
   *
   * @see Locale#toLanguageTag()
   */
  public static Map<String, String> mergeTexts(Map<String, String> fromTexts, Map<String, String> targetTexts) {
    fromTexts = ObjectUtility.nvl(fromTexts, Collections.emptyMap());
    targetTexts = ObjectUtility.nvl(targetTexts, Collections.emptyMap());

    // Merge maps
    Map<String, String> result = new HashMap<>();
    result.putAll(targetTexts);
    result.putAll(fromTexts);

    // Remove unnecessary entries
    result.entrySet().removeIf(entry -> {
      String key = entry.getKey();
      String value = entry.getValue();
      while (key.contains("-")) {
        key = key.substring(0, key.lastIndexOf("-"));
        if (result.containsKey(key)) {
          // found an existing entry with a shorter language tag -> remove longer entry if the values are the same, otherwise keep both
          return Objects.equals(result.get(key), value);
        }
      }
      return false; // keep entry
    });

    return result;
  }
}
