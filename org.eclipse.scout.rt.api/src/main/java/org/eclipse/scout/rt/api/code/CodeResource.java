/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.code;

import static java.util.Collections.emptySet;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.scout.rt.api.data.code.CodeDo;
import org.eclipse.scout.rt.api.data.code.CodeTypeDo;
import org.eclipse.scout.rt.api.data.code.CodeTypeRequest;
import org.eclipse.scout.rt.api.data.code.IApiExposedCodeTypeDoProvider;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.rest.IRestResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("codes")
public class CodeResource implements IRestResource {

  private static final Logger LOG = LoggerFactory.getLogger(CodeResource.class);

  /**
   * Gets all CodeTypes which should be published to the UI on application startup (bootstrap)
   *
   * @param allLanguages
   *          {@code true} if all application languages should be exported. {@code false} if only the texts for the
   *          current {@link NlsLocale} should be part of the response. Customize {@link #getApplicationLanguages()} to
   *          specify the supported application languages.
   * @return List of the CodeTypes.
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<CodeTypeDo> list(@QueryParam("allLanguages") @DefaultValue("true") boolean allLanguages, CodeTypeRequest request) {
    Set<String> ids = request == null ? null : request.getCodeTypeIds();
    Map<String, CodeTypeDo> codeTypeDos = getCodeTypesById(ids);
    if (allLanguages) {
      loadOtherTextsFor(codeTypeDos, ids);
    }
    return codeTypeDos.values();
  }

  protected Map<String, CodeTypeDo> getCodeTypesById(Set<String> ids) {
    Set<CodeTypeDo> codeTypes = BEANS.optional(IApiExposedCodeTypeDoProvider.class)
        .map(provider -> provider.provide(ids))
        .orElse(emptySet()); // if no provider is available, no CodeTypes can be available, because no shared is available.
    return convertToMap(codeTypes);
  }

  protected Map<String, CodeTypeDo> convertToMap(Collection<CodeTypeDo> codeTypes) {
    Map<String, CodeTypeDo> result = new HashMap<>(codeTypes.size());
    codeTypes.forEach(codeTypeDo -> putToMap(codeTypeDo, result));
    return result;
  }

  protected void putToMap(CodeTypeDo codeType, Map<String, CodeTypeDo> map) {
    if (codeType == null) {
      return;
    }
    String id = codeType.getId();
    if (!StringUtility.hasText(id)) {
      LOG.warn("Skipping CodeType without id.");
      return; // skip CodeType without id
    }
    CodeTypeDo previous = map.put(id, codeType);
    if (previous != null) {
      LOG.warn("CodeType with duplicate id '{}' found.", id);
    }
  }

  protected void loadOtherTextsFor(Map<String, CodeTypeDo> codeTypeMap, Set<String> ids) {
    Set<Locale> otherAppLanguages = new HashSet<>(getApplicationLanguages());
    otherAppLanguages.remove(NlsLocale.get()); // remove current running locale (is already part of the result)
    if (otherAppLanguages.isEmpty()) {
      return; // no other languages available
    }

    otherAppLanguages.stream()
        // order by language tag, within a language, locales without country come first ([de, en_US, de_DE, en, en_GB] -> [de, de_DE, en, en_GB, en_US])
        .sorted(Comparator.comparing(Locale::toLanguageTag))
        .forEachOrdered(otherLanguage -> {
          // switch locale without creating a new RunContext in order to share the current transaction, especially an
          // already leased DB connection
          final Locale backup = NlsLocale.get();
          NlsLocale.set(otherLanguage);
          try {
            getCodeTypesById(ids).values().forEach(otherCodeTypeDo -> mergeCodeTypeTexts(otherCodeTypeDo, codeTypeMap));
          }
          finally {
            NlsLocale.set(backup);
          }
        });
  }

  protected void mergeCodeTypeTexts(CodeTypeDo from, Map<String, CodeTypeDo> targetMap) {
    if (from == null) {
      return;
    }
    CodeTypeDo target = targetMap.get(from.getId());
    if (target == null) {
      return;
    }
    target.withTexts(mergeTexts(from.getTexts(), target.getTexts()));
    target.withTextsPlural(mergeTexts(from.getTextsPlural(), target.getTextsPlural()));
    Collection<CodeDo> targetChildren = getChildCodes(target);
    from.getCodes().forEach(code -> mergeCodeTexts(code, targetChildren));
  }

  protected void mergeCodeTexts(CodeDo from, Collection<CodeDo> targetList) {
    if (from == null) {
      return;
    }
    Object codeId = from.getId();
    CodeDo target = targetList.stream()
        .filter(code -> Objects.equals(code.getId(), codeId))
        .findAny().orElse(null);
    if (target == null) {
      return;
    }
    target.withTexts(mergeTexts(from.getTexts(), target.getTexts()));
    Collection<CodeDo> targetChildren = getChildCodes(target);
    from.getChildren().forEach(code -> mergeCodeTexts(code, targetChildren));
  }

  /**
   * Merges two text maps (languageTag to text). If there is already an entry in the target map with the same text and a
   * languageTag that is a prefix of the entry in the from map, this entry is skipped as this text is considered to be
   * inherited. <br>
   * Examples:
   * <ul>
   * <li>merge {de_DE=groß} into {de=groß} => {de=groß}</li>
   * <li>merge {de_CH=gross} into {de=groß} => {de=groß, de_CH=gross}</li>
   * <li>merge {de=ok} into {en=ok} => {de=ok, en=ok}</li>
   * </ul>
   */
  protected Map<String /* languageTag */, String /* text */> mergeTexts(Map<String /* languageTag */, String /* text */> fromTexts, Map<String /* languageTag */, String /* text */> targetTexts) {
    if (CollectionUtility.isEmpty(targetTexts)) {
      // no targetTexts -> return fromTexts
      return fromTexts;
    }

    var result = new HashMap<>(targetTexts);

    if (CollectionUtility.isEmpty(fromTexts)) {
      // no fromTexts
      return result;
    }

    fromTexts.entrySet()
        .stream()
        .filter(entry -> result.entrySet().stream()
            // check if there is already an entry with a languageTag that is a prefix to the current one ...
            .noneMatch(targetEntry -> StringUtility.startsWith(entry.getKey(), targetEntry.getKey())
                // ... and the same text
                && ObjectUtility.equals(entry.getValue(), targetEntry.getValue())))
        .forEach(entry -> result.put(entry.getKey(), entry.getValue()));

    return result;
  }

  protected Collection<CodeDo> getChildCodes(CodeTypeDo parent) {
    return parent.getCodes();
  }

  protected Collection<CodeDo> getChildCodes(CodeDo parent) {
    return parent.getChildren();
  }

  protected Collection<Locale> getApplicationLanguages() {
    return Collections.singleton(NlsLocale.get());
  }
}
