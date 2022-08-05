/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ObjectFactory} from '@eclipse-scout/core';
import * as self from './index.js';

export {default as SvgField} from './svg/SvgField';
export {default as SvgFieldAdapter} from './svg/SvgFieldAdapter';

export default self;
ObjectFactory.get().registerNamespace('scout', self);
