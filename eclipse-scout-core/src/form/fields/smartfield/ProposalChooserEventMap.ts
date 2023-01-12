/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, LookupRow, PropertyChangeEvent, ProposalChooser, ProposalChooserContent, ProposalChooserContentRow, SmartFieldActiveFilter, Status, WidgetEventMap} from '../../../index';

export interface ProposalChooserActiveFilterSelectedEvent<TValue = any,
  TContent extends ProposalChooserContent = any,
  TContentRow extends ProposalChooserContentRow<TValue> = any,
  T = ProposalChooser<TValue, TContent, TContentRow>> extends Event<T> {
  activeFilter: SmartFieldActiveFilter;
}

export interface ProposalChooserLookupRowSelectedEvent<TValue = any,
  TContent extends ProposalChooserContent = any,
  TContentRow extends ProposalChooserContentRow<TValue> = any,
  T = ProposalChooser<TValue, TContent, TContentRow>> extends Event<T> {
  lookupRow: LookupRow<TValue>;
}

export interface ProposalChooserEventMap<TValue, TContent extends ProposalChooserContent, TContentRow extends ProposalChooserContentRow<TValue>> extends WidgetEventMap {
  'activeFilterSelected': ProposalChooserActiveFilterSelectedEvent<TValue, TContent, TContentRow>;
  'lookupRowSelected': ProposalChooserLookupRowSelectedEvent<TValue, TContent, TContentRow>;
  'propertyChange:status': PropertyChangeEvent<Status>;
}
