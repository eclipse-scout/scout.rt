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
import java.util.stream.Collectors;

import org.eclipse.scout.rt.api.data.code.CodeDo;
import org.eclipse.scout.rt.api.data.ApiExposeHelper;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;

@Bean
public class CodeDoConverter {
  /**
   * Converts the data of the given Code to a {@link IDoEntity}.
   * <p>
   * Exported Format:
   *
   * <pre>
   * {
   *   id: any,
   *   objectType: '',
   *   modelClass: '',
   *   active: true,
   *   enabled: true,
   *   iconId: '',
   *   tooltipText: '',
   *   backgroundColor: '',
   *   foregroundColor: '',
   *   font: '',
   *   cssClass: '',
   *   extKey: '',
   *   value: 789,
   *   partitionId: 587,
   *   texts: {},
   *   children: [{ ... }]
   * }
   * </pre>
   *
   * @return The data of the given Code converted to a {@link IDoEntity}.
   */
  public <CODE_ID> CodeDo convert(ICode<CODE_ID> code) {
    if (code == null) {
      return null;
    }

    String id = convertId(code.getId());
    if (id == null) {
      return null; // id is mandatory
    }

    List<CodeDo> codes = codesToDos(getChildCodesToExport(code));
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
    ICodeType<?, CODE_ID> codeType = code.getCodeType();

    CodeDo codeDo = createCodeDo(code).withId(id);
    if (!active) {
      codeDo.withActive(active);
    }
    if (!enabled) {
      codeDo.withEnabled(enabled);
    }
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
      codeDo.withSortCode(codeType.getCodeIndex(code));
    }
    BEANS.get(ApiExposeHelper.class).setObjectTypeToDo(code, codeDo);
    return codeDo;
  }

  protected CodeDo createCodeDo(ICode<?> code) {
    CodeDo codeDo = code.newCodeDo();
    if (codeDo != null) {
      return codeDo;
    }
    return BEANS.get(CodeDo.class);
  }

  protected String computeFieldName(ICode<?> code) {
    String fieldName = BEANS.get(ApiExposeHelper.class).fieldNameOf(code);
    if (fieldName != null) {
      return fieldName;
    }
    String simpleName = code.getClass().getSimpleName();
    return StringUtility.lowercaseFirst(StringUtility.removeSuffixes(simpleName, "Code"));
  }

  public List<CodeDo> codesToDos(List<? extends ICode<?>> codesToExport) {
    if (CollectionUtility.isEmpty(codesToExport)) {
      return Collections.emptyList();
    }
    return codesToExport.stream()
        .filter(Objects::nonNull)
        .map(ICode::toDo)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  protected String convertId(Object id) {
    return BEANS.get(CodeTypeDoConverter.class).convertId(id);
  }

  protected List<? extends ICode<?>> getChildCodesToExport(ICode<?> code) {
    return code.getChildCodes();
  }
}
