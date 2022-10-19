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
import {PropertyChangeEvent, Status, ValueFieldEventMap} from '../../../index';
import {ValueFieldAcceptInputEvent} from '../ValueFieldEventMap';

export interface DateFieldAcceptInputEvent extends ValueFieldAcceptInputEvent<Date> {
  errorStatus: Status;
  value: Date;
}

export default interface DateFieldEventMap extends ValueFieldEventMap<Date> {
  'acceptInput': DateFieldAcceptInputEvent;
  'propertyChange:allowedDates': PropertyChangeEvent<Date[]>;
  'propertyChange:autoDate': PropertyChangeEvent<Date>;
  'propertyChange:dateFocused': PropertyChangeEvent<boolean>;
  'propertyChange:dateFormatPattern': PropertyChangeEvent<string>;
  'propertyChange:dateHasText': PropertyChangeEvent<boolean>;
  'propertyChange:hasDate': PropertyChangeEvent<boolean>;
  'propertyChange:hasTime': PropertyChangeEvent<boolean>;
  'propertyChange:timeFocused': PropertyChangeEvent<boolean>;
  'propertyChange:timeFormatPattern': PropertyChangeEvent<string>;
  'propertyChange:timeHasText': PropertyChangeEvent<boolean>;
  'propertyChange:timePickerResolution': PropertyChangeEvent<number>;
}
