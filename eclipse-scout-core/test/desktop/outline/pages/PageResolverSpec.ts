/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DataObjectInventory, ObjectFactory, PageIdDummyPageParamDo, PageModel, PageParamDo, PageResolver, PageWithNodes, scout, Session, typeName} from '../../../../src';

describe('PageResolver', () => {
  let session: Session;

  class PageWithoutParam extends PageWithNodes {
    protected override _jsonModel(): PageModel {
      return {
        uuid: 'a9fd0bab-2d13-428d-be77-7ce41031c9cf'
      };
    }
  }

  class PageWithoutParamAndUuid extends PageWithNodes {
  }

  class MyPage extends PageWithNodes {
    declare pageParam: MyPageParamDo;
  }

  @typeName('pageResolve.MyPageParam')
  class MyPageParamDo extends PageParamDo {
    prop: string;
  }

  @typeName('pageResolve.ParamWithoutPageParam')
  class ParamWithoutPageParamDo extends PageParamDo {
    prop: string;
  }

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    DataObjectInventory.get().add(MyPageParamDo);
    ObjectFactory.get().registerNamespace('pageResolve', {PageWithoutParam, PageWithoutParamAndUuid, MyPage, MyPageParamDo, ParamWithoutPageParamDo});
  });

  afterEach(() => {
    DataObjectInventory.get().remove(MyPageParamDo);
    ObjectFactory.get().removeFromNamespace([PageWithoutParam, PageWithoutParamAndUuid, MyPage, MyPageParamDo, ParamWithoutPageParamDo]);
  });

  describe('findObjectTypeForPageParam', () => {
    it('finds pages using a PageIdDummyPageParamDo', () => {
      const pageResolver = PageResolver.get(session);
      let pageParam = scout.create(PageIdDummyPageParamDo, {
        pageId: 'a9fd0bab-2d13-428d-be77-7ce41031c9cf'
      });
      expect(pageResolver.findObjectTypeForPageParam(pageParam)).toBe('pageResolve.PageWithoutParam');

      let emptyPageParam = scout.create(PageIdDummyPageParamDo);
      expect(pageResolver.findObjectTypeForPageParam(emptyPageParam)).toBe(null);

      let pageParamWrongId = scout.create(PageIdDummyPageParamDo, {
        pageId: 'xxxxxxx-1234-428d-be77-7ce41031c9cf'
      });
      expect(pageResolver.findObjectTypeForPageParam(pageParamWrongId)).toBe(null);
    });

    it('finds pages using naming convention', () => {
      const pageResolver = PageResolver.get(session);
      let pageParam = scout.create(MyPageParamDo);
      expect(pageResolver.findObjectTypeForPageParam(pageParam)).toBe('pageResolve.MyPage');

      let pageParamWithoutPage = scout.create(ParamWithoutPageParamDo);
      expect(pageResolver.findObjectTypeForPageParam(pageParamWithoutPage)).toBe(null);
    });

    it('returns null if no pageParam is passed', () => {
      const pageResolver = PageResolver.get(session);
      expect(pageResolver.findObjectTypeForPageParam(null)).toBe(null);
    });
  });
});
