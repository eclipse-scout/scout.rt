/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ObjectFactory} from '@eclipse-scout/core';
import * as self from './index';

export * from './svg/SvgField';
export * from './svg/SvgFieldModel';
export * from './svg/SvgFieldEventMap';
export * from './svg/SvgFieldAdapter';

export default self;

ObjectFactory.get().registerNamespace('scout', self);
