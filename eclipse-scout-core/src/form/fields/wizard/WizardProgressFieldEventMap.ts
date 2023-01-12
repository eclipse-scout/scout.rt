/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, FormFieldEventMap, PropertyChangeEvent, WizardProgressField, WizardStep} from '../../../index';

export interface WizardProgressFieldStepActionEvent<T = WizardProgressField> extends Event<T> {
  stepIndex: number;
}

export interface WizardProgressFieldEventMap extends FormFieldEventMap {
  'stepAction': WizardProgressFieldStepActionEvent;
  'propertyChange:activeStepIndex': PropertyChangeEvent<number>;
  'propertyChange:steps': PropertyChangeEvent<WizardStep[]>;
}
