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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.scout.rt.api.data.code.CodeDo;
import org.eclipse.scout.rt.api.data.code.CodeTypeDo;
import org.eclipse.scout.rt.api.data.code.IApiExposedCodeTypeContributor;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.rest.IRestResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

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
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<CodeTypeDo> list(@QueryParam("allLanguages") @DefaultValue("true") boolean allLanguages) {
    Map<String, CodeTypeDo> codeTypeDos = getCodeTypesById();
    if (allLanguages) {
      loadOtherTextsFor(codeTypeDos);
    }
    return codeTypeDos.values();
  }

  protected Map<String, CodeTypeDo> getCodeTypesById() {
    Set<CodeTypeDo> codeTypes = new HashSet<>();
    BEANS.all(IApiExposedCodeTypeContributor.class).forEach(contributor -> contributor.contribute(codeTypes));
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

  protected void loadOtherTextsFor(Map<String, CodeTypeDo> codeTypeMap) {
    Set<Locale> otherAppLanguages = new HashSet<>(getApplicationLanguages());
    otherAppLanguages.remove(NlsLocale.get()); // remove current running locale (is already part of the result)
    if (otherAppLanguages.isEmpty()) {
      return; // no other languages available
    }

    for (Locale otherLanguage : otherAppLanguages) {
      RunContexts.copyCurrent()
          .withLocale(otherLanguage)
          .call(this::getCodeTypesById).values()
          .forEach(otherCodeTypeDo -> mergeCodeTypeTexts(otherCodeTypeDo, codeTypeMap));
    }
  }

  protected void mergeCodeTypeTexts(CodeTypeDo from, Map<String, CodeTypeDo> targetMap) {
    if (from == null) {
      return;
    }
    CodeTypeDo target = targetMap.get(from.getId());
    if (target == null) {
      return;
    }
    from.getTexts().forEach(target::withText);
    from.getTextsPlural().forEach(target::withTextPlural);
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
    from.getTexts().forEach(target::withText);
    Collection<CodeDo> targetChildren = getChildCodes(target);
    from.getChildren().forEach(code -> mergeCodeTexts(code, targetChildren));
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
