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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.api.data.ApiExposeHelper;
import org.eclipse.scout.rt.api.data.code.CodeDo;
import org.eclipse.scout.rt.api.data.code.CodeTypeDo;
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

/**
 * Base implementation of {@link ICodeToDoFunction}. It transfers all attributes from {@link ICode} to the corresponding
 * attribute in {@link CodeDo}.
 */
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

  /**
   * Converts the codes given to data objects.
   *
   * @param codesToExport
   *     The codes to export.
   * @return The created data objects as immutable {@link List}. The list may contain fewer elements than the input
   * collection if a {@link ICode} cannot be converted to a data object or has no valid id.
   */
  public List<CodeDo> codesToDos(Collection<? extends ICode<?>> codesToExport) {
    if (CollectionUtility.isEmpty(codesToExport)) {
      return Collections.emptyList();
    }
    return codesToExport.stream()
        .filter(Objects::nonNull)
        .map(ICode::toDo)
        .filter(Objects::nonNull)
        .filter(code -> code.getId() != null) // id is mandatory
        .collect(Collectors.toList());
  }

  /**
   * @return the {@link Function} used to convert a Code id to a {@link String}. By default {@link #convertId(Object)}
   *         is used.
   */
  public Function<Object, String> getIdConverter() {
    return m_idConverter;
  }

  /**
   * Changes the {@link Function} used to convert a Code id to a {@link String}. By default {@link #convertId(Object)}
   * is used. May be used to change the conversion logic application wide.
   *
   * @param idConverter
   *          The new conversion {@link Function}. {@code null} values are ignored.
   */
  public void setIdConverter(Function<Object, String> idConverter) {
    if (idConverter == null) {
      return;
    }
    m_idConverter = idConverter;
  }

  /**
   * converts a Code id ({@link ICode#getId()}) or CodeType id ({@link ICodeType#getId()}) to a {@link String} which can
   * be put to a {@link CodeDo} or {@link CodeTypeDo}.
   * <p>
   * All {@link ICodeTypeDoIdConverter} instances are asked first to convert the id. If none can handle the value,
   * {@link Object#toString()} will be invoked.
   *
   * @param id
   *          The id to convert or {@code null}.
   * @return The {@link String} representation of the id or {@code null} if the id was {@code null}.
   * @see ICodeTypeDoIdConverter
   */
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

  /**
   * Adapter interface that converts a Code id ({@link ICode#getId()}) or CodeType id ({@link ICodeType#getId()}) to a
   * {@link String} which can be put to a {@link CodeDo} or {@link CodeTypeDo}. The converter should return {@code null}
   * for values it cannot handle (unsupported data type). The first (according to bean order, see {@link Order})
   * non-null conversion will be used as result.
   */
  @Bean
  public interface ICodeTypeDoIdConverter extends Function<Object, String> {
  }

  /**
   * Default Code id converter supporting {@link IId IIds}.
   */
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
