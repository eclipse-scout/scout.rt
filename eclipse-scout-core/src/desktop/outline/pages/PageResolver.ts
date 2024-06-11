/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, BaseDoEntity, Constructor, DoRegistry, ObjectFactory, Outline, Page, PageIdDummyPageParamDo, PageParamDo, scout, strings} from '../../../index';
import $ from 'jquery';

export class PageResolver {

  protected static _INSTANCE: PageResolver = null;
  protected pageByPageParam: Map<Constructor<PageParamDo>, Constructor<Page>> = null;

  findObjectTypeForPageParam(pageParam: PageParamDo): string {
    if (!pageParam) {
      return null;
    }

    // PageIdDummyPageParamDo
    if (PageIdDummyPageParamDo.TYPE_NAME === pageParam._type) {
      const dummyPageParamDo = pageParam as PageIdDummyPageParamDo;
      return this._findObjectTypeForPageParam(dummyPageParamDo);
    }

    // By explicit decorator
    const pageParamConstructor = this._assertPageParamConstructor(pageParam);
    const pageObjectType = this._findObjectTypeForPageParamConstructor(pageParamConstructor);
    if (pageObjectType) {
      return pageObjectType;
    }

    // By naming convention
    const pageParamObjectType = pageParam.objectType || ObjectFactory.get().getObjectType(pageParamConstructor);
    if (pageParamObjectType?.endsWith('PageParamDo')) {
      const pageName = strings.removeSuffix(pageParamObjectType, 'ParamDo');
      const pageExists = !!ObjectFactory.get().resolveTypedObjectType(pageName);
      if (pageExists) {
        return pageName;
      }
    }
    return null;
  }

  protected _assertPageParamConstructor(pageParamOrModel: PageParamDo): Constructor<PageParamDo> {
    if (pageParamOrModel instanceof BaseDoEntity) {
      return pageParamOrModel.constructor as Constructor<PageParamDo>;
    }
    return DoRegistry.get().toConstructor(pageParamOrModel._type);
  }

  protected _findObjectTypeForPageParamConstructor(paramConstructor: Constructor<PageParamDo>): string {
    let pageConstructor = this.getPageByParam(paramConstructor);
    if (!pageConstructor) {
      return null;
    }
    return ObjectFactory.get().getObjectType(pageConstructor);
  }

  protected getPageByParam(paramConstructor: Constructor<PageParamDo>): Constructor<Page> {
    if (!paramConstructor) {
      return null;
    }
    if (!this.pageByPageParam) {
      const mapping = new Map<Constructor<PageParamDo>, Constructor<Page>>();
      const allPageClasses = ObjectFactory.get().getClassesInstanceOf(Page);
      for (let PageConstructor of allPageClasses) {
        let pageParamType = (new PageConstructor()).pageParamType;
        if (pageParamType !== null && pageParamType !== PageIdDummyPageParamDo) {
          mapping.set(pageParamType, PageConstructor);
        }
      }
      this.pageByPageParam = mapping;
    }
    return this.pageByPageParam.get(paramConstructor);
  }

  protected _findObjectTypeForPageParam(pageParam: PageIdDummyPageParamDo): string {
    if (!pageParam) {
      return null;
    }
    const allPageClasses = ObjectFactory.get().getClassesInstanceOf(Page);
    const parent = scout.create(Outline, {parent: App.get().sessions[0].desktop});
    try {
      for (let candidate of allPageClasses) {
        const objectType = this._getObjectTypeForPageIfParamMatches(parent, pageParam, candidate);
        if (objectType) {
          return objectType;
        }
      }
      return null;
    } finally {
      parent.destroy();
    }
  }

  protected _getObjectTypeForPageIfParamMatches(parent: Outline, param: PageIdDummyPageParamDo, PageConstructor: Constructor<Page>): string {
    let page: Page = null;
    try {
      page = new PageConstructor();
      if (page.pageParamType !== PageIdDummyPageParamDo && page.pageParamType !== null) {
        return null;
      }
      page.loadFromModel({parent});
      if (page.matchesPageParam(param)) {
        return ObjectFactory.get().getObjectType(page.constructor as Constructor);
      }
      return null;
    } catch (e) {
      const objectType = ObjectFactory.get().getObjectType(PageConstructor);
      $.log.info(`Unable to create and initialize ${objectType}. Cannot check for PageParam`);
    } finally {
      if (page) {
        page.destroy();
      }
    }
  }

  static get(): PageResolver {
    if (!PageResolver._INSTANCE) {
      PageResolver._INSTANCE = scout.create(PageResolver);
    }
    return PageResolver._INSTANCE;
  }
}
