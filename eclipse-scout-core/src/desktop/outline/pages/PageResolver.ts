/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, Constructor, InitModelOf, ObjectFactory, ObjectModel, objects, ObjectType, ObjectWithType, Page, PageIdDummyPageParamDo, PageParamDo, scout, Session, strings, TypeDescriptor} from '../../../index';

export class PageResolver implements PageResolverModel, ObjectWithType {
  declare model: PageResolverModel;
  session: Session;
  objectType: string;

  protected static _INSTANCES: Map<Session, PageResolver> = new Map();

  /**
   * A map of uuid to objectType of the pages using a {@link PageIdDummyPageParamDo}.
   */
  protected _objectTypeByUuid: Map<string, ObjectType<Page>> = new Map();

  init(model: InitModelOf<this>) {
    this.session = scout.assertProperty(model, 'session', Session);
  }

  /**
   * @returns the object type of the page matching the given page param.
   */
  findObjectTypeForPageParam(pageParam: PageParamDo): ObjectType<Page> {
    if (!pageParam) {
      return null;
    }

    // By Dummy Page Param
    if (pageParam instanceof PageIdDummyPageParamDo) {
      const dummyPageParamDo = pageParam as PageIdDummyPageParamDo;
      return this._findObjectTypeForPageParam(dummyPageParamDo);
    }

    // By naming convention
    const pageParamConstructor = pageParam.constructor as Constructor<PageParamDo>;
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

  protected _findObjectTypeForPageParam(pageParam: PageIdDummyPageParamDo): ObjectType<Page> {
    if (!pageParam) {
      return null;
    }

    let objectType = this._objectTypeByUuid.get(pageParam.pageId);
    if (!objectType) {
      // If the page is not yet in the cache, (re-)init the cache first
      this._initObjectTypeByUuid();
    }
    return this._objectTypeByUuid.get(pageParam.pageId) ?? null;
  }

  protected _initObjectTypeByUuid() {
    for (const PageConstructor of ObjectFactory.get().getSubClassesOf(Page)) {
      let pageUuid = this._getPageUuid(PageConstructor);
      if (pageUuid) {
        let objectType = ObjectFactory.get().getObjectType(PageConstructor);
        this._objectTypeByUuid.set(pageUuid, objectType);
      }
    }
  }

  protected _getPageUuid(PageConstructor: Constructor<Page>): string {
    let page: Page = null;
    try {
      page = new PageConstructor();
      page.minimalInit();
      if (page.pageParam instanceof PageIdDummyPageParamDo) {
        return page.pageParam.pageId;
      }
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
