/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Event, LookupRow, PropertyChangeEvent, ProposalChooser, Status, WidgetEventMap} from '../../../index';
import {ProposalChooserContent, ProposalChooserContentRow} from './ProposalChooser';
import {SmartFieldActiveFilter} from './SmartField';

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

export default interface ProposalChooserEventMap<TValue, TContent extends ProposalChooserContent, TContentRow extends ProposalChooserContentRow<TValue>> extends WidgetEventMap {
  'activeFilterSelected': ProposalChooserActiveFilterSelectedEvent<TValue, TContent, TContentRow>;
  'lookupRowSelected': ProposalChooserLookupRowSelectedEvent<TValue, TContent, TContentRow>;
  'propertyChange:status': PropertyChangeEvent<Status>;
}
