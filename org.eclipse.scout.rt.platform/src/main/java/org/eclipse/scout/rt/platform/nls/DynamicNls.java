/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.nls;

import static java.util.Collections.unmodifiableSet;
import static org.eclipse.scout.rt.platform.util.CollectionUtility.orderedHashSetWithoutNullElements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.Bean;

@Bean
public class DynamicNls {

  private final List<NlsResourceBundleCache> m_resourceBundles;
  private final Set<ITextPostProcessor> m_textPostProcessors;

  public DynamicNls() {
    m_resourceBundles = new ArrayList<>();
    m_textPostProcessors = new LinkedHashSet<>();
  }

  public void registerResourceBundle(String resourceBundleName, Class<?> wrapperClass) {
    m_resourceBundles.add(0, new NlsResourceBundleCache(resourceBundleName, wrapperClass));
  }

  /**
   * Appends a new {@link ITextPostProcessor} to this instance. This processor will be invoked on each text retrieval.
   *
   * @param postProcessor
   *     The new processor. Nothing is appended if it is {@code null}.
   * @return This {@link DynamicNls} instance.
   */
  public DynamicNls withTextPostProcessor(ITextPostProcessor postProcessor) {
    if (postProcessor != null) {
      m_textPostProcessors.add(postProcessor);
    }
    return this;
  }

  /**
   * Appends the {@link ITextPostProcessor text post processors} given to this instance. These processors will be
   * invoked on each text retrieval.
   *
   * @param processors
   *     The new processors. {@code null} elements are skipped.
   * @return This {@link DynamicNls} instance.
   */
  public DynamicNls withTextPostProcessors(Collection<? extends ITextPostProcessor> processors) {
    clearTextPostProcessors();
    if (processors != null && !processors.isEmpty()) {
      m_textPostProcessors.addAll(orderedHashSetWithoutNullElements(processors));
    }
    return this;
  }

  /**
   * Removes all {@link ITextPostProcessor} elements for which the {@link Predicate} given returns {@code true}.
   *
   * @param predicate
   *     The predicate to execute. This method does nothing if the predicate is {@code null}.
   * @return {@code true} if any elements were removed.
   */
  public boolean removeTextPostProcessor(Predicate<? super ITextPostProcessor> predicate) {
    if (predicate == null) {
      return false;
    }
    return m_textPostProcessors.removeIf(predicate);
  }

  /**
   * Removes the {@link ITextPostProcessor} given.
   *
   * @param postProcessor
   *     The processor to remove.
   * @return {@code true} if any elements were removed.
   */
  public boolean removeTextPostProcessor(ITextPostProcessor postProcessor) {
    if (postProcessor == null) {
      return false;
    }
    Predicate<ITextPostProcessor> pred = processor -> processor == postProcessor;
    return removeTextPostProcessor(pred);
  }

  /**
   * Removes all {@link ITextPostProcessor text post processors} from this instance.
   *
   * @return This {@link DynamicNls} instance.
   */
  public DynamicNls clearTextPostProcessors() {
    m_textPostProcessors.clear();
    return this;
  }

  /**
   * @return An unmodifiable {@link Set} of all {@link ITextPostProcessor} instances registered. Never returns
   * {@code null}. {@link Collections#unmodifiableSet(Set)} preservers the given order.
   */
  public Collection<ITextPostProcessor> getTextPostProcessors() {
    return unmodifiableSet(m_textPostProcessors);
  }

  /**
   * @param key
   *     nls text key
   * @param messageArguments
   *     the translation of the text might contain variables {0},{1},{2},... Examples: getText("MissingFile1");
   *     with translation: MissingFile1=Das File konnte nicht gefunden werden getText("MissingFile2",fileName);
   *     with translation: MissingFile2=Das File {0} konnte nicht gefunden werden.
   *     getText("MissingFile3",fileName,dir); with translation: MissingFile3=Das File {0} im Ordner {1} konnte
   *     nicht gefunden werden
   */
  public String getText(String key, String... messageArguments) {
    return getText(null, key, messageArguments);
  }

  /**
   * Resolves the given {@code key} and returns its assigned text. Applies all internally cached
   * {@link ITextPostProcessor text post processors}.
   *
   * @param locale
   *     the locale of the text
   * @param key
   *     nls text key
   * @param messageArguments
   *     the translation of the text might contain variables {0},{1},{2},... Examples: getText("MissingFile1");
   *     with translation: MissingFile1=Das File konnte nicht gefunden werden getText("MissingFile2",fileName);
   *     with translation: MissingFile2=Das File {0} konnte nicht gefunden werden.
   *     getText("MissingFile3",fileName,dir); with translation: MissingFile3=Das File {0} im Ordner {1} konnte
   *     nicht gefunden werden
   */
  public String getText(Locale locale, String key, String... messageArguments) {
    if (key == null) {
      return null;
    }
    if (locale == null) {
      locale = getDefaultLocale();
    }
    String text = getTextInternal(locale, key);
    text = NlsUtility.bindText(text, messageArguments);
    return NlsUtility.postProcessText(locale, key, text, getTextPostProcessors(), messageArguments);
  }

  protected String getTextInternal(Locale locale, String key) {
    for (NlsResourceBundleCache c : m_resourceBundles) {
      NlsResourceBundle resourceBundle = c.getResourceBundle(locale);
      if (resourceBundle != null) {
        String result = resourceBundle.getText(key);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  /**
   * get all key/texts defined or redefined by the wrapper class for that locale
   */
  public Map<String, String> getTextMap(Locale locale) {
    if (locale == null) {
      locale = getDefaultLocale();
    }

    Map<String, String> map = new HashMap<>();
    for (NlsResourceBundleCache c : m_resourceBundles) {
      NlsResourceBundle resourceBundle = c.getResourceBundle(locale);
      if (resourceBundle != null) {
        resourceBundle.collectTextMapping(map::putIfAbsent);
      }
    }
    return map;
  }

  /**
   * Override this method to change default locale behavior
   */
  protected Locale getDefaultLocale() {
    return NlsLocale.get();
  }
}
