/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.code;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.eclipse.scout.rt.api.data.code.CodeDo;
import org.eclipse.scout.rt.api.data.code.CodeTypeDo;
import org.eclipse.scout.rt.api.data.ApiExposeHelper;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.id.IdCodec;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.StringUtility;

@Bean
public class CodeTypeDoConverter {

  /**
   * converts the data of the given CodeType to a {@link IDoEntity}.
   * <p>
   * Resulting DoEntity Format:
   *
   * <pre>
   * {
   *   id: '1234',
   *   objectType: '',
   *   modelClass: '',
   *   iconId: '',
   *   text: '',
   *   textPlural: '',
   *   isHierarchical: true,
   *   maxLevel: 4,
   *   codes: [...]
   * }
   * </pre>
   *
   * @return The data of the given CodeType (included contained Codes) converted to a {@link IDoEntity}.
   */
  public CodeTypeDo convert(ICodeType<?, ?> codeType) {
    if (codeType == null) {
      return null;
    }

    String id = convertId(codeType.getId());
    if (id == null) {
      return null; // id is mandatory
    }

    String currentLanguageTag = NlsLocale.get().toLanguageTag();
    String iconId = codeType.getIconId();
    List<CodeDo> codes = BEANS.get(CodeDoConverter.class).codesToDos(getCodesToExport(codeType));
    String text = codeType.getText();
    String textPlural = codeType.getTextPlural();
    boolean isHierarchy = codeType.isHierarchy();
    int maxLevel = codeType.getMaxLevel();

    CodeTypeDo codeTypeDo = createCodeTypeDo(codeType).withId(id);
    if (StringUtility.hasText(text)) {
      codeTypeDo.withText(currentLanguageTag, text);
    }
    if (StringUtility.hasText(textPlural)) {
      codeTypeDo.withTextPlural(currentLanguageTag, textPlural);
    }
    if (StringUtility.hasText(iconId)) {
      codeTypeDo.withIconId(iconId);
    }
    if (isHierarchy) {
      codeTypeDo.withIsHierarchical(isHierarchy);
    }
    if (maxLevel != Integer.MAX_VALUE) {
      codeTypeDo.withMaxLevel(maxLevel);
    }
    if (!codes.isEmpty()) {
      codeTypeDo.withCodes(codes);
    }
    if (Platform.get().inDevelopmentMode()) {
      codeTypeDo.withModelClass(codeType.getClass().getName());
    }
    BEANS.get(ApiExposeHelper.class).setObjectTypeToDo(codeType, codeTypeDo);
    return codeTypeDo;
  }

  protected CodeTypeDo createCodeTypeDo(ICodeType<?, ?> codeType) {
    return BEANS.get(CodeTypeDo.class);
  }

  protected List<? extends ICode<?>> getCodesToExport(ICodeType<?, ?> codeType) {
    return codeType.getCodes();
  }

  public String convertId(Object id) {
    if (id == null) {
      return null;
    }
    return BEANS.all(ICodeTypeDoIdConverter.class).stream()
        .map(converter -> converter.apply(id))
        .filter(Objects::nonNull)
        .findFirst()
        .orElseGet(() -> id.toString());
  }

  @Bean
  public interface ICodeTypeDoIdConverter extends Function<Object, String> {
  }

  @Order(-5000)
  public static class IIdCodeTypeDoIdConverter implements ICodeTypeDoIdConverter {
    @Override
    public String apply(Object id) {
      if (id instanceof IId) {
        return BEANS.get(IdCodec.class).toQualified((IId) id);
      }
      return null;
    }
  }
}
