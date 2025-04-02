/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.bookmark;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.BookmarkDoBuilderOptions")
public class BookmarkDoBuilderOptionsDo extends DoEntity {

  public DoValue<Boolean> createOutline() {
    return doValue("createOutline");
  }

  public DoValue<Boolean> persistableRequired() {
    return doValue("persistableRequired");
  }

  public DoValue<Boolean> fallbackAllowed() {
    return doValue("fallbackAllowed");
  }

  public DoValue<Boolean> createTitle() {
    return doValue("createTitle");
  }

  public DoValue<Boolean> createDescription() {
    return doValue("createDescription");
  }

  public DoValue<Boolean> createTablePreferences() {
    return doValue("createTablePreferences");
  }

  public DoValue<Boolean> createTableRowSelections() {
    return doValue("createTableRowSelections");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public BookmarkDoBuilderOptionsDo withCreateOutline(Boolean createOutline) {
    createOutline().set(createOutline);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getCreateOutline() {
    return createOutline().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isCreateOutline() {
    return nvl(getCreateOutline());
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BookmarkDoBuilderOptionsDo withPersistableRequired(Boolean persistableRequired) {
    persistableRequired().set(persistableRequired);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getPersistableRequired() {
    return persistableRequired().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isPersistableRequired() {
    return nvl(getPersistableRequired());
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BookmarkDoBuilderOptionsDo withFallbackAllowed(Boolean fallbackAllowed) {
    fallbackAllowed().set(fallbackAllowed);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getFallbackAllowed() {
    return fallbackAllowed().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isFallbackAllowed() {
    return nvl(getFallbackAllowed());
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BookmarkDoBuilderOptionsDo withCreateTitle(Boolean createTitle) {
    createTitle().set(createTitle);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getCreateTitle() {
    return createTitle().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isCreateTitle() {
    return nvl(getCreateTitle());
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BookmarkDoBuilderOptionsDo withCreateDescription(Boolean createDescription) {
    createDescription().set(createDescription);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getCreateDescription() {
    return createDescription().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isCreateDescription() {
    return nvl(getCreateDescription());
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BookmarkDoBuilderOptionsDo withCreateTablePreferences(Boolean createTablePreferences) {
    createTablePreferences().set(createTablePreferences);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getCreateTablePreferences() {
    return createTablePreferences().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isCreateTablePreferences() {
    return nvl(getCreateTablePreferences());
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BookmarkDoBuilderOptionsDo withCreateTableRowSelections(Boolean createTableRowSelections) {
    createTableRowSelections().set(createTableRowSelections);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getCreateTableRowSelections() {
    return createTableRowSelections().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isCreateTableRowSelections() {
    return nvl(getCreateTableRowSelections());
  }
}
