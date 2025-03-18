/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {BaseDoEntity, Constructor, MaxRowCountContributionDo, objects, PageParamDo} from '../index';

export class BookmarkAdapter {

  pageParamsMatch(pageParam1: PageParamDo, pageParam2: PageParamDo) {
    if (!pageParam1 && !pageParam2) {
      return true;
    }
    if (!pageParam1 || !pageParam2) {
      return false;
    }

    pageParam1 = this.normalizePageParam(pageParam1);
    pageParam2 = this.normalizePageParam(pageParam2);

    return objects.equals(pageParam1, pageParam2);
  }

  normalizePageParam(pageParam: PageParamDo): PageParamDo {
    pageParam = pageParam.clone();
    for (const contribution of this.getIgnoredContributionClassesForPageParamComparison()) {
      pageParam.removeContribution(contribution);
    }
    return pageParam;
  }

  getIgnoredContributionClassesForPageParamComparison(): Constructor<BaseDoEntity>[] {
    return [MaxRowCountContributionDo]; // FIXME CGU [js-bookmark] verify if this is really necessary
  }
}
