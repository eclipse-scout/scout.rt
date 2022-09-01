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

import {Column, PropertyChangeEvent, PropertyEventMap} from '../../index';
import {HorizontalAlignment} from '../../cell/Cell';

export default interface ColumnEventMap extends PropertyEventMap {
  'propertyChange:autoOptimizeWidth': PropertyChangeEvent<boolean, Column>;
  'propertyChange:compacted': PropertyChangeEvent<boolean, Column>;
  'propertyChange:cssClass': PropertyChangeEvent<string, Column>;
  'propertyChange:displayable': PropertyChangeEvent<boolean, Column>;
  'propertyChange:editable': PropertyChangeEvent<boolean, Column>;
  'propertyChange:headerCssClass': PropertyChangeEvent<string, Column>;
  'propertyChange:headerHtmlEnabled': PropertyChangeEvent<boolean, Column>;
  'propertyChange:headerIconId': PropertyChangeEvent<string, Column>;
  'propertyChange:headerTooltipHtmlEnabled': PropertyChangeEvent<boolean, Column>;
  'propertyChange:headerTooltipText': PropertyChangeEvent<string, Column>;
  'propertyChange:horizontalAlignment': PropertyChangeEvent<HorizontalAlignment, Column>;
  'propertyChange:mandatory': PropertyChangeEvent<boolean, Column>;
  'propertyChange:maxLength': PropertyChangeEvent<number, Column>;
  'propertyChange:text': PropertyChangeEvent<string, Column>;
  'propertyChange:textWrap': PropertyChangeEvent<boolean, Column>;
  'propertyChange:visible': PropertyChangeEvent<boolean, Column>;
  'propertyChange:width': PropertyChangeEvent<number, Column>;
}
