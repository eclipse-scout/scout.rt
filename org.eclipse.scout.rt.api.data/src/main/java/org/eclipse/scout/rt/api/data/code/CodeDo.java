/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.code;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.Code")
public class CodeDo extends DoEntity {

  public CodeDo() {
    withTexts(new HashMap<>());
  }

  public DoValue<String> id() {
    return doValue("id");
  }

  public DoValue<String> objectType() {
    return doValue("objectType");
  }

  public DoValue<String> modelClass() {
    return doValue("modelClass");
  }

  public DoValue<Boolean> active() {
    return doValue("active");
  }

  public DoValue<Boolean> enabled() {
    return doValue("enabled");
  }

  public DoValue<String> iconId() {
    return doValue("iconId");
  }

  public DoValue<String> tooltipText() {
    return doValue("tooltipText");
  }

  public DoValue<String> backgroundColor() {
    return doValue("backgroundColor");
  }

  public DoValue<String> foregroundColor() {
    return doValue("foregroundColor");
  }

  public DoValue<String> font() {
    return doValue("font");
  }

  public DoValue<String> cssClass() {
    return doValue("cssClass");
  }

  public DoValue<String> extKey() {
    return doValue("extKey");
  }

  public DoValue<Number> value() {
    return doValue("value");
  }

  public DoValue<Long> partitionId() {
    return doValue("partitionId");
  }

  public DoValue<Integer> sortCode() {
    return doValue("sortCode");
  }

  public DoValue<String> fieldName() {
    return doValue("fieldName");
  }

  /**
   * @return The texts as {@link Map}. Key is the {@link Locale#toLanguageTag() languageTag}, value the text for this
   * language.
   */
  public DoValue<Map<String, String>> texts() {
    return doValue("texts");
  }

  public DoList<CodeDo> children() {
    return doList("children");
  }

  /* **************************************************************************
   * CUSTOM CONVENIENCE METHODS
   * *************************************************************************/

  public CodeDo withText(String languageTag, String text) {
    getTexts().put(languageTag, text);
    return this;
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public CodeDo withId(String id) {
    id().set(id);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getId() {
    return id().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CodeDo withObjectType(String objectType) {
    objectType().set(objectType);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getObjectType() {
    return objectType().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CodeDo withModelClass(String modelClass) {
    modelClass().set(modelClass);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getModelClass() {
    return modelClass().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CodeDo withActive(Boolean active) {
    active().set(active);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getActive() {
    return active().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isActive() {
    return nvl(getActive());
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CodeDo withEnabled(Boolean enabled) {
    enabled().set(enabled);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getEnabled() {
    return enabled().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isEnabled() {
    return nvl(getEnabled());
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CodeDo withIconId(String iconId) {
    iconId().set(iconId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getIconId() {
    return iconId().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CodeDo withTooltipText(String tooltipText) {
    tooltipText().set(tooltipText);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getTooltipText() {
    return tooltipText().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CodeDo withBackgroundColor(String backgroundColor) {
    backgroundColor().set(backgroundColor);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getBackgroundColor() {
    return backgroundColor().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CodeDo withForegroundColor(String foregroundColor) {
    foregroundColor().set(foregroundColor);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getForegroundColor() {
    return foregroundColor().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CodeDo withFont(String font) {
    font().set(font);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getFont() {
    return font().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CodeDo withCssClass(String cssClass) {
    cssClass().set(cssClass);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getCssClass() {
    return cssClass().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CodeDo withExtKey(String extKey) {
    extKey().set(extKey);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getExtKey() {
    return extKey().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CodeDo withValue(Number value) {
    value().set(value);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Number getValue() {
    return value().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CodeDo withPartitionId(Long partitionId) {
    partitionId().set(partitionId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Long getPartitionId() {
    return partitionId().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CodeDo withSortCode(Integer sortCode) {
    sortCode().set(sortCode);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Integer getSortCode() {
    return sortCode().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CodeDo withFieldName(String fieldName) {
    fieldName().set(fieldName);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getFieldName() {
    return fieldName().get();
  }

  /**
   * See {@link #texts()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public CodeDo withTexts(Map<String, String> texts) {
    texts().set(texts);
    return this;
  }

  /**
   * See {@link #texts()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public Map<String, String> getTexts() {
    return texts().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CodeDo withChildren(Collection<? extends CodeDo> children) {
    children().updateAll(children);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CodeDo withChildren(CodeDo... children) {
    children().updateAll(children);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<CodeDo> getChildren() {
    return children().get();
  }
}
