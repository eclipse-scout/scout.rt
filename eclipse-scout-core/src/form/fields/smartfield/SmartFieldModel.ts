/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CodeType, ColumnDescriptor, LookupCallOrModel, LookupRow, SmartFieldActiveFilter, SmartFieldDisplayStyle, ValueFieldModel} from '../../../index';

export interface SmartFieldModel<TValue> extends ValueFieldModel<TValue> {
  /**
   * Configures the {@link LookupCall} that is used to load the proposals.
   */
  lookupCall?: LookupCallOrModel<TValue>;
  /**
   * If set, a {@link CodeLookupCall} is created and used for the property {@link lookupCall}.
   *
   * The property accepts a {@link CodeType} class or a {@link CodeType.id} (see {@link CodeTypeCache.get}).
   */
  codeType?: string | (new() => CodeType<TValue>);
  lookupRow?: LookupRow<TValue>;
  /**
   * Configures whether the proposals should be shown hierarchically using a {@link TreeProposalChooser}.
   *
   * Default is false.
   */
  browseHierarchy?: boolean;
  /**
   * Configures the maximum number of proposals.
   *
   * It needs to be a positive number, _not_ null or undefined.
   *
   * Default is {@link SmartField.DEFAULT_BROWSE_MAX_COUNT}.
   */
  browseMaxRowCount?: number;
  /**
   * Configures whether the proposals should be expanded automatically.
   *
   * The property only has an effect if {@link browseHierarchy} is true.
   *
   * Default is true.
   */
  browseAutoExpandAll?: boolean;
  /**
   * Configures whether the proposals should be loaded incrementally.
   *
   * The property only has an effect if {@link browseHierarchy} is true.
   *
   * Default is false.
   */
  browseLoadIncremental?: boolean;
  /**
   * Configures whether proposals should only be shown if a text search string has been entered.
   *
   * Set this property to true if you expect a large amount of data for an unconstrained search.
   *
   * Default value is false.
   */
  searchRequired?: boolean;
  /**
   * Configures whether the {@link ProposalChooser.activeFilterGroup} should be shown.
   *
   * If the group is shown, the user can choose whether all, only the active or only the inactive proposals should be displayed.
   *
   * Default is false.
   */
  activeFilterEnabled?: boolean;
  /**
   * Configures the default value of the {@link ProposalChooser.activeFilterGroup}.
   *
   * Only has an effect if {@link activeFilterEnabled} is set tot true.
   */
  activeFilter?: SmartFieldActiveFilter;
  /**
   * Configures the labels used by the {@link ProposalChooser.activeFilterGroup}.
   *
   * If no labels are specified, default labels are used.
   *
   * Only has an effect if {@link activeFilterEnabled} is set tot true.
   */
  activeFilterLabels?: string[];
  /**
   * Configures the columns used by the {@link TableProposalChooser}.
   *
   * The property only has an effect if {@link browseHierarchy} is set to false, because otherwise a {@link Tree} is used instead of a {@link Table}.
   *
   * When the returned value is `null`, the table proposal chooser has only one column (showing the lookup row text) without column header.
   * To change this default behavior, return an array of {@link ColumnDescriptor}s.
   */
  columnDescriptors?: ColumnDescriptor[];
  /**
   * Configures the {@link DisplayStyle} of the smart field.
   *
   * Default is {@link SmartField.DisplayStyle.DEFAULT}.
   */
  displayStyle?: SmartFieldDisplayStyle;
  /**
   * Configures whether the smart field should be displayed in a touch friendly mode.
   *
   * If the touch friendly mode is active, touching the smart field opens a popup on top of the screen to minimize overlapping issues with the on-screen keyboard.
   * The user can search for and select proposals only in that popup.
   *
   * Default is false.
   */
  touchMode?: boolean;
  /**
   * Configures the maximum number of characters the user can enter.
   *
   * Default is 500.
   */
  maxLength?: number;
}
