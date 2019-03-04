/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.res.loader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.nls.NlsResourceBundle;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.text.AbstractDynamicNlsTextProviderService;
import org.eclipse.scout.rt.platform.util.FileUtility;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.UiLocalesProperty;
import org.json.JSONObject;

public class TextsLoader extends AbstractResourceLoader {

  @Override
  public BinaryResource loadResource(String pathInfo) throws IOException {
    List<Locale> languageLocales = getLanguageLocales();
    JSONObject jsonTexts = new JSONObject();

    // Gather all texts and group them by language tags
    Map<Locale, Map<String, String>> textsByLanguageTag = new LinkedHashMap<>();
    for (AbstractDynamicNlsTextProviderService textService : BEANS.all(AbstractDynamicNlsTextProviderService.class)) {
      for (Locale locale : languageLocales) {
        NlsResourceBundle bundle = getResourceBundle(textService, locale);
        if (bundle == null) {
          continue;
        }

        Map<String, String> map = textsByLanguageTag.computeIfAbsent(locale, k -> new TreeMap<>());
        map.putAll(bundle.getTextMap());
      }
    }

    // Convert the texts into json
    for (Entry<Locale, Map<String, String>> entry : textsByLanguageTag.entrySet()) {
      Locale locale = entry.getKey();
      String languageTag = (locale == null || locale == Locale.ROOT) ? "default" : locale.toLanguageTag();

      JSONObject jsonTextMap = textsToJson(languageTag, entry.getValue());
      jsonTexts.put(languageTag, jsonTextMap);
    }

    // Create a binary resource
    byte[] content = jsonTexts.toString(2).getBytes(StandardCharsets.UTF_8);
    return BinaryResources.create()
        .withFilename(pathInfo)
        .withCharset(StandardCharsets.UTF_8)
        .withContentType(FileUtility.getContentTypeForExtension("json"))
        .withContent(content)
        .withCachingAllowed(true)
        .build();
  }

  protected JSONObject textsToJson(String languageTag, Map<String, String> textMap) {
    JSONObject texts = new JSONObject();
    for (Entry<String, String> entry : textMap.entrySet()) {
      texts.put(entry.getKey(), entry.getValue());
    }
    return texts;
  }

  protected List<Locale> getLanguageLocales() {
    List<String> languageTags = CONFIG.getPropertyValue(UiLocalesProperty.class);
    return processLanguageTags(languageTags);
  }

  /**
   * Processes the given language tags and does two things:
   * <ul>
   * <li>Adds a null value at the beginning representing the default locale</li>
   * <li>Makes sure the language itself without locale is always added.</li>
   * </ul>
   * Always adding the language is necessary, because if the texts for de-CH should be loaded, the texts for de need to
   * be loaded as well. Because texts for de-CH only contain the exceptions to de.<br>
   * You may now say "this should be configured properly". But: the configuration property is used to load the locales
   * as well, and to eventually display the available locales to the user. This gives the possibility to only display a
   * subset of Locales (like English, Deutsch (Schweiz)), instead of (English, Deutsch, Deutsch (Schweiz)).
   *
   * @return a new list of language tags including the missing tags in the same order as the original one
   */
  protected List<Locale> processLanguageTags(List<String> languageTags) {
    // Group by language and add language itself (e.g [en,de-CH] will be converted to en: [en], de: [de,de-CH]
    Map<String, Set<Locale>> languages = new LinkedHashMap<>();
    for (String tag : languageTags) {
      Locale locale = Locale.forLanguageTag(tag);
      Set<Locale> localesForLanguage = languages.get(locale.getLanguage());
      if (localesForLanguage == null) {
        localesForLanguage = new LinkedHashSet<>();
        languages.put(locale.getLanguage(), localesForLanguage);

        // Always add language itself, without any country
        localesForLanguage.add(new Locale(locale.getLanguage()));
      }
      localesForLanguage.add(locale);
    }

    // Create a new list including the missing language tags
    List<Locale> locales = languages.values().stream().flatMap(Set::stream).collect(Collectors.toList());

    // add default language
    locales.add(0, Locale.ROOT);
    return locales;
  }

  protected NlsResourceBundle getResourceBundle(AbstractDynamicNlsTextProviderService textService, Locale locale) {
    return NlsResourceBundle.getBundle(null, textService.getDynamicNlsBaseName(), locale, textService.getClass().getClassLoader());
  }
}
