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
import {ColumnModel, LookupCall, LookupCallModel} from '../../index';
import {ObjectType} from '../../ObjectFactory';

export default interface SmartColumnModel extends ColumnModel {
  /**
   * @see codes.get
   */
  codeType?: string;
  /**
   * Reference to LookupCall, a LookupCallModel or a string to a LookupCall class.
   */
  lookupCall?: LookupCall<any> | LookupCallModel<any> & { objectType: ObjectType<LookupCall<any>> } | string;
  browseHierarchy?: boolean;
  browseMaxRowCount?: number;
  browseAutoExpandAll?: boolean;
  browseLoadIncremental?: boolean;
  activeFilterEnabled?: boolean;
}
