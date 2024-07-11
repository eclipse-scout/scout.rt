/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.code.mapping;

import java.util.List;
import java.util.function.Function;

import org.eclipse.scout.rt.api.data.ApiExposeHelper;
import org.eclipse.scout.rt.api.data.code.CodeDo;
import org.eclipse.scout.rt.api.data.code.CodeTypeDo;
import org.eclipse.scout.rt.dataobject.mapping.AbstractToDoFunction;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;

/**
 * Base implementation of {@link ICodeTypeToDoFunction}. It transfers all attributes from {@link ICodeType} to the
 * corresponding attribute in {@link CodeTypeDo}.
 */
public abstract class AbstractCodeTypeToDoFunction<EXPLICIT_SOURCE extends ICodeType<?, ?>, EXPLICIT_TARGET extends CodeTypeDo>
    extends AbstractToDoFunction<EXPLICIT_SOURCE, EXPLICIT_TARGET, ICodeType<?, ?>, CodeTypeDo>
    implements ICodeTypeToDoFunction {

  private Function<Object, String> m_idConverter;

  protected AbstractCodeTypeToDoFunction() {
    m_idConverter = AbstractCodeToDoFunction::convertId;
  }

  @Override
  public void apply(EXPLICIT_SOURCE codeType, EXPLICIT_TARGET codeTypeDo) {
    String id = getIdConverter().apply(codeType.getId());
    String currentLanguageTag = NlsLocale.get().toLanguageTag();
    String iconId = codeType.getIconId();
    List<CodeDo> codes = BEANS.get(CodeToDoFunction.class).codesToDos(getCodesToConvert(codeType));
    String text = codeType.getText();
    String textPlural = codeType.getTextPlural();
    boolean isHierarchy = codeType.isHierarchy();
    int maxLevel = codeType.getMaxLevel();

    codeTypeDo.withId(id);
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
      codeTypeDo.withHierarchical(isHierarchy);
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
  }

  protected List<? extends ICode<?>> getCodesToConvert(EXPLICIT_SOURCE codeType) {
    return codeType.getCodes(false);
  }

  /**
   * @return the {@link Function} used to convert a CodeType id to a {@link String}. By default
   * {@link AbstractCodeToDoFunction#convertId(Object)} is used.
   * @see AbstractCodeToDoFunction#convertId(Object)
   */
  public Function<Object, String> getIdConverter() {
    return m_idConverter;
  }

  /**
   * Changes the {@link Function} used to convert a CodeType id to a {@link String}. By default
   * {@link AbstractCodeToDoFunction#convertId(Object)} is used. May be used to change the conversion logic application
   * wide.
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
}
