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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.api.data.ApiExposeHelper;
import org.eclipse.scout.rt.api.data.code.CodeDo;
import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.id.IdCodec;
import org.eclipse.scout.rt.dataobject.mapping.AbstractToDoFunction;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;

public abstract class AbstractCodeToDoFunction<EXPLICIT_SOURCE extends ICode<?>, EXPLICIT_TARGET extends CodeDo>
    extends AbstractToDoFunction<EXPLICIT_SOURCE, EXPLICIT_TARGET, ICode<?>, CodeDo>
    implements ICodeToDoFunction {

  private Function<Object, String> m_idConverter;

  protected AbstractCodeToDoFunction() {
    m_idConverter = AbstractCodeToDoFunction::convertId;
  }

  @Override
  public void apply(EXPLICIT_SOURCE code, EXPLICIT_TARGET codeDo) {
    String id = getIdConverter().apply(code.getId());
    List<CodeDo> codes = codesToDos(getChildCodesToConvert(code));
    boolean active = code.isActive();
    boolean enabled = code.isEnabled();
    String iconId = code.getIconId();
    String tooltipText = code.getTooltipText();
    String backgroundColor = code.getBackgroundColor();
    String foregroundColor = code.getForegroundColor();
    String cssClass = code.getCssClass();
    String extKey = code.getExtKey();
    Number value = code.getValue();
    long partitionId = code.getPartitionId();
    String text = code.getText();
    FontSpec font = code.getFont();
    String fieldName = computeFieldName(code);
    ICodeType codeType = code.getCodeType();

    codeDo
        .withId(id)
        .withActive(active)
        .withEnabled(enabled);
    if (StringUtility.hasText(iconId)) {
      codeDo.withIconId(iconId);
    }
    if (StringUtility.hasText(tooltipText)) {
      codeDo.withTooltipText(tooltipText);
    }
    if (StringUtility.hasText(backgroundColor)) {
      codeDo.withBackgroundColor(backgroundColor);
    }
    if (StringUtility.hasText(foregroundColor)) {
      codeDo.withForegroundColor(foregroundColor);
    }
    if (StringUtility.hasText(cssClass)) {
      codeDo.withCssClass(cssClass);
    }
    if (StringUtility.hasText(extKey)) {
      codeDo.withExtKey(extKey);
    }
    if (value != null) {
      codeDo.withValue(value);
    }
    if (partitionId != 0) {
      codeDo.withPartitionId(partitionId);
    }
    if (StringUtility.hasText(text)) {
      codeDo.withText(NlsLocale.get().toLanguageTag(), text);
    }
    if (!codes.isEmpty()) {
      codeDo.withChildren(codes);
    }
    if (font != null) {
      codeDo.withFont(font.toPattern());
    }
    if (Platform.get().inDevelopmentMode()) {
      codeDo.withModelClass(code.getClass().getName());
    }
    if (StringUtility.hasText(fieldName)) {
      codeDo.withFieldName(fieldName);
    }
    if (codeType != null) {
      //noinspection unchecked
      codeDo.withSortCode(codeType.getCodeIndex(code));
    }
    BEANS.get(ApiExposeHelper.class).setObjectTypeToDo(code, codeDo);
  }

  protected String computeFieldName(EXPLICIT_SOURCE code) {
    String fieldName = BEANS.get(ApiExposeHelper.class).fieldNameOf(code);
    if (fieldName != null) {
      return fieldName;
    }
    String simpleName = code.getClass().getSimpleName();
    return StringUtility.lowercaseFirst(StringUtility.removeSuffixes(simpleName, "Code"));
  }

  protected List<? extends ICode<?>> getChildCodesToConvert(EXPLICIT_SOURCE code) {
    return code.getChildCodes();
  }

  public List<CodeDo> codesToDos(List<? extends ICode<?>> codesToExport) {
    if (CollectionUtility.isEmpty(codesToExport)) {
      return Collections.emptyList();
    }
    return codesToExport.stream()
        .filter(Objects::nonNull)
        .filter(code -> code.getId() != null) // id is mandatory
        .map(ICode::toDo)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  public Function<Object, String> getIdConverter() {
    return m_idConverter;
  }

  public void setIdConverter(Function<Object, String> idConverter) {
    if (idConverter == null) {
      return;
    }
    m_idConverter = idConverter;
  }

  public static String convertId(Object id) {
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
