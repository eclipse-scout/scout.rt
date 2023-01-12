/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {PropertyChangeEvent, Status, ValueFieldAcceptInputEvent, ValueFieldEventMap} from '../../../index';

export interface DateFieldAcceptInputEvent extends ValueFieldAcceptInputEvent<Date> {
  errorStatus: Status;
  value: Date;
}

export interface DateFieldEventMap extends ValueFieldEventMap<Date> {
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
