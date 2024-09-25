/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html;

import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;

public class UiTextContributor implements IUiTextContributor {

  @Override
  public void contribute(Set<String> textKeys) {
    // Automatically include all texts of the org.eclipse.scout.rt.ui.html module
    textKeys.addAll(BEANS.get(UiTextProviderService.class).getTextMap(null).keySet());

    // Additional text keys from org.eclipse.scout.rt.nls
    textKeys.addAll(List.of(
        "Cancel",
        "CancelButton",
        "CloseButton",
        "Column",
        "ColumnSorting",
        "CorrelationId",
        "DateIsNotAllowed",
        "ErrorWhileLoadingData",
        "ExtendedSearchAddAdditionalOrMenu",
        "ExtendedSearchAddEitherOrMenu",
        "FormEmptyMandatoryFieldsMessage",
        "FormInvalidFieldsMessage",
        "FormInvalidFieldsWarningMessage",
        "FormSaveChangesQuestion",
        "ConfirmApplyChanges",
        "GroupBy",
        "NoGrouping",
        "InactiveState",
        "InvalidNumberMessageX",
        "InvalidValueMessageX",
        "NavigationBackward",
        "NetSystemsNotAvailable",
        "No",
        "NoButton",
        "Ok",
        "OkButton",
        "PleaseTryAgainLater",
        "ProceedAnyway",
        "Remove",
        "ResetButton",
        "ResetTableColumns",
        "SaveButton",
        "Search",
        "SearchButton",
        "ShowColumns",
        "SmartFieldCannotComplete",
        "SmartFieldInactiveRow",
        "SmartFieldMoreThanXRows",
        "SmartFieldNoDataFound",
        "SortBy",
        "TooManyRows",
        "SmartFieldNotUnique",
        "Yes",
        "YesButton",
        "NumberTooLargeMessageXY",
        "NumberTooLargeMessageX",
        "NumberTooSmallMessageXY",
        "NumberTooSmallMessageX",
        "UnsavedChangesTitle",
        "SaveChangesOfSelectedItems",
        "CheckAll",
        "UncheckAll",
        "TheRequestedResourceCouldNotBeFound",
        "MaxOutlineRowWarningMobileWithEstimatedRowCount",
        "MaxOutlineRowWarningMobile",
        "MaxOutlineRowWarningWithEstimatedRowCount",
        "MaxOutlineRowWarning",
        "FormsCannotBeSaved",
        "NotAllCheckedFormsCanBeSaved",
        "FormValidationFailedTitle",
        "YouAreNotAuthorizedToPerformThisAction"));
  }
}
