/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Constructor, DoValueMetaData, doValueMetaData} from '../../index';

export interface DoTypeResolver {
  resolve(rawObj: Record<string, any>, metaData: DoValueMetaData): Constructor;
}

export class DefaultDoTypeResolver implements DoTypeResolver {
  resolve(rawObj: Record<string, any>, metaData: DoValueMetaData): Constructor {
    return doValueMetaData.chooseDataObjectType(rawObj, metaData);
  }
}
