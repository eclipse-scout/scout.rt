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

@TypeName("scout.CodeType")
public class CodeTypeDo extends DoEntity {

  public CodeTypeDo() {
    withTexts(new HashMap<>());
    withTextsPlural(new HashMap<>());
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

  public DoValue<String> iconId() {
    return doValue("iconId");
  }

  /**
   * @return The texts as {@link Map}. Key is the {@link Locale#toLanguageTag() languageTag}, value the text for this
   * language.
   */
  public DoValue<Map<String, String>> texts() {
    return doValue("texts");
  }

  /**
   * @return The texts as {@link Map}. Key is the {@link Locale#toLanguageTag() languageTag}, value the text for this
   * language.
   */
  public DoValue<Map<String, String>> textsPlural() {
    return doValue("textsPlural");
  }

  public DoValue<Boolean> hierarchical() {
    return doValue("hierarchical");
  }

  public DoValue<Integer> maxLevel() {
    return doValue("maxLevel");
  }

  public DoList<CodeDo> codes() {
    return doList("codes");
  }

  /* **************************************************************************
   * CUSTOM CONVENIENCE METHODS
   * *************************************************************************/

  public CodeTypeDo withText(String languageTag, String text) {
    getTexts().put(languageTag, text);
    return this;
  }

  public CodeTypeDo withTextPlural(String languageTag, String text) {
    getTextsPlural().put(languageTag, text);
    return this;
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public CodeTypeDo withId(String id) {
    id().set(id);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getId() {
    return id().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CodeTypeDo withObjectType(String objectType) {
    objectType().set(objectType);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getObjectType() {
    return objectType().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CodeTypeDo withModelClass(String modelClass) {
    modelClass().set(modelClass);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getModelClass() {
    return modelClass().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CodeTypeDo withIconId(String iconId) {
    iconId().set(iconId);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getIconId() {
    return iconId().get();
  }

  /**
   * See {@link #texts()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public CodeTypeDo withTexts(Map<String, String> texts) {
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

  /**
   * See {@link #textsPlural()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public CodeTypeDo withTextsPlural(Map<String, String> textsPlural) {
    textsPlural().set(textsPlural);
    return this;
  }

  /**
   * See {@link #textsPlural()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public Map<String, String> getTextsPlural() {
    return textsPlural().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CodeTypeDo withHierarchical(Boolean hierarchical) {
    hierarchical().set(hierarchical);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getHierarchical() {
    return hierarchical().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isHierarchical() {
    return nvl(getHierarchical());
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CodeTypeDo withMaxLevel(Integer maxLevel) {
    maxLevel().set(maxLevel);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Integer getMaxLevel() {
    return maxLevel().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CodeTypeDo withCodes(Collection<? extends CodeDo> codes) {
    codes().updateAll(codes);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public CodeTypeDo withCodes(CodeDo... codes) {
    codes().updateAll(codes);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<CodeDo> getCodes() {
    return codes().get();
  }
}
