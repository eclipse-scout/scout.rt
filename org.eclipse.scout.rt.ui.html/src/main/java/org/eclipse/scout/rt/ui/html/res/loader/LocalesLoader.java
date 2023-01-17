/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.res.loader;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.util.FileUtility;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.UiLocalesProperty;
import org.eclipse.scout.rt.ui.html.json.JsonLocale;
import org.json.JSONArray;
import org.json.JSONObject;

public class LocalesLoader extends AbstractResourceLoader {

  @Override
  public BinaryResource loadResource(String pathInfo) {
    List<String> languageTags = getLanguageTags();

    JSONArray jsonLocales = new JSONArray();
    for (String tag : languageTags) {
      jsonLocales.put(jsonLocale(tag));
    }

    byte[] localeBytes = jsonLocales.toString(2).getBytes(StandardCharsets.UTF_8);
    return BinaryResources.create()
        .withFilename(pathInfo)
        .withCharset(StandardCharsets.UTF_8)
        .withContentType(FileUtility.getContentTypeForExtension("json"))
        .withContent(localeBytes)
        .withCachingAllowed(true)
        .build();
  }

  public static List<String> getLanguageTags() {
    List<String> languageTags = CONFIG.getPropertyValue(UiLocalesProperty.class);
    if (languageTags.size() == 1 && "all".equals(languageTags.get(0))) {
      languageTags = getAvailableLanguageTags();
    }
    return languageTags;
  }

  protected JSONObject jsonLocale(String languageTag) {
    Locale locale = Locale.forLanguageTag(languageTag);
    return JsonLocale.toJson(locale);
  }

  public static List<String> getAvailableLanguageTags() {
    return Arrays.stream(Locale.getAvailableLocales()).map(Locale::toLanguageTag).collect(Collectors.toList());
  }
}
