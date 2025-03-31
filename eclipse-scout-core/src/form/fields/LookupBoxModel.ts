/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CodeType, LookupCallOrModel, ObjectOrChildModel, ValueFieldModel, Widget} from '../../index';

export interface LookupBoxModel<TLookup, TValue = TLookup[]> extends ValueFieldModel<TValue, TLookup | TValue> {
  /**
   * Configures a box that is shown below the list box and can be used to display filter options.
   */
  filterBox?: ObjectOrChildModel<Widget>;
  /**
   * Configures the {@link LookupCall} that is used to load the data.
   */
  lookupCall?: LookupCallOrModel<TLookup>;
  /**
   * If set, a {@link CodeLookupCall} is created and used for the property {@link lookupCall}.
   *
   * The property accepts a {@link CodeType} class or a {@link CodeType.id} (see {@link CodeTypeCache.get}).
   */
  codeType?: string | (new() => CodeType<TLookup>);
}
