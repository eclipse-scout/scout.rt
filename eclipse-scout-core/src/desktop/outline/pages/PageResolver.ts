/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, Constructor, InitModelOf, ObjectFactory, ObjectModel, objects, ObjectWithType, Page, PageIdDummyPageParamDo, PageParamDo, scout, Session, strings, TypeDescriptor} from '../../../index';

export class PageResolver implements PageResolverModel, ObjectWithType {
  declare model: PageResolverModel;
  session: Session;
  objectType: string;

  protected static _INSTANCES: Map<Session, PageResolver> = new Map();
  protected pageByPageParam: Map<Constructor<PageParamDo>, Constructor<Page>> = null;

  init(model: InitModelOf<this>) {
    this.session = scout.assertProperty(model, 'session', Session);
  }

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
    const pageParamConstructor = pageParam.constructor as Constructor<PageParamDo>;
    const pageObjectType = this._findObjectTypeForPageParamConstructor(pageParamConstructor);
    if (pageObjectType) {
      return pageObjectType;
    }

    // By naming convention
    const pageParamObjectType = ObjectFactory.get().getObjectType(pageParamConstructor);
    if (pageParamObjectType?.endsWith('PageParamDo')) {
      const pageName = strings.removeSuffix(pageParamObjectType, 'ParamDo');
      const pageExists = !!TypeDescriptor.resolveType(pageName);
      if (pageExists) {
        return pageName;
      }
    }
    return null;
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
      const allPageClasses = ObjectFactory.get().getSubClassesOf(Page);
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
    const allPageClasses = ObjectFactory.get().getSubClassesOf(Page);
    for (let candidate of allPageClasses) {
      const objectType = this._getObjectTypeForPageIfParamMatches(pageParam, candidate);
      if (objectType) {
        return objectType;
      }
    }
    return null;
  }

  protected _getObjectTypeForPageIfParamMatches(param: PageIdDummyPageParamDo, PageConstructor: Constructor<Page>): string {
    let page: Page = null;
    try {
      page = new PageConstructor();
      if (page.pageParamType !== PageIdDummyPageParamDo && page.pageParamType !== null) {
        return null;
      }
      page.minimalInit();
      if (page.matchesPageParam(param)) {
        return ObjectFactory.get().getObjectType(page.constructor as Constructor);
      }
      return null;
    } catch (e) {
      const objectType = ObjectFactory.get().getObjectType(PageConstructor);
      let message = `Unable to create and initialize ${objectType}. Cannot check for PageParam. Error: ${e.message}`;
      $.log.error(message);
      this.session.sendLogRequest(message);
    } finally {
      if (page) {
        page.destroy();
      }
    }
  }

  static get(session?: Session): PageResolver {
    session = scout.nvl(session, App.get().sessions[0]);
    return objects.getOrSetIfAbsent(PageResolver._INSTANCES, session, () => scout.create(PageResolver, {session}));
  }
}

export interface PageResolverModel extends ObjectModel<PageResolver> {
  session: Session;
}
