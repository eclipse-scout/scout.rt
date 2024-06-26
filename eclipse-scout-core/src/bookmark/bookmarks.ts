/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, DoEntity, DoRegistry, objects, PageParamDo, scout, typeName} from '../index';

@typeName('crm.Bookmark')
export class BookmarkDo extends BaseDoEntity {
  key: string;
  titles: Record<string, string>;
  description: string;
  definition: IBookmarkDefinitionDo;
}

// --------------------------------------------------

export interface IBookmarkDefinitionDo {
  bookmarkedPage: IBookmarkPageDo;
}

@typeName('crm.OutlineBookmarkDefinition')
export class OutlineBookmarkDefinitionDo extends BaseDoEntity implements IBookmarkDefinitionDo {
  bookmarkedPage: IBookmarkPageDo;
  outlineId: string;
  /** Path from the outline's root to the {@link bookmarkedPage} */
  pagePath: IBookmarkPageDo[];
}

@typeName('crm.PageBookmarkDefinition')
export class PageBookmarkDefinitionDo extends BaseDoEntity implements IBookmarkDefinitionDo {
  bookmarkedPage: IBookmarkPageDo;
}

// --------------------------------------------------

export interface IBookmarkPageDo {
  pageParam: PageParamDo;
  displayText: string;
}

@typeName('crm.NodeBookmarkPage')
export class NodeBookmarkPageDo extends BaseDoEntity implements IBookmarkPageDo {
  pageParam: PageParamDo;
  displayText: string;
}

@typeName('crm.BookmarkTableRowIdentifier')
export class BookmarkTableRowIdentifierDo extends BaseDoEntity {
  keyComponents: IBookmarkTableRowIdentifierComponentDo[];
}

@typeName('crm.TableBookmarkPage')
export class TableBookmarkPageDo extends BaseDoEntity implements IBookmarkPageDo {
  pageParam: PageParamDo;
  displayText: string;
  expandedChildRow: BookmarkTableRowIdentifierDo;
  selectedChildRows: BookmarkTableRowIdentifierDo[];
  tablePreferences: DoEntity; // FIXME bsh [js-bookmark] TableClientUiPreferencesDo;
  searchFilterComplete: boolean; // FIXME bsh [js-bookmark] always true?
  searchData: DoEntity; // FIXME bsh [js-bookmark] ISearchDo;
  chartTableControlConfig: DoEntity; // FIXME bsh [js-bookmark] ChartTableControlConfigDo;
}

export interface IBookmarkTableRowIdentifierComponentDo {
}

/**
 * Never serialize this!
 */
@typeName('crm.BookmarkTableRowIdentifierObjectComponent')
export class BookmarkTableRowIdentifierObjectComponentDo extends BaseDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: any;
}

@typeName('crm.BookmarkTableRowIdentifierEntityKeyComponent')
export class BookmarkTableRowIdentifierEntityKeyComponentDo extends BaseDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: string;
}

@typeName('crm.BookmarkTableRowIdentifierDateComponent')
export class BookmarkTableRowIdentifierDateComponentDo extends BaseDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: string;
}

@typeName('crm.BookmarkTableRowIdentifierBooleanComponent')
export class BookmarkTableRowIdentifierBooleanComponentDo extends BaseDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: boolean;
}

@typeName('crm.BookmarkTableRowIdentifierIntegerComponent')
export class BookmarkTableRowIdentifierIntegerComponentDo extends BaseDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: number;
}

@typeName('crm.BookmarkTableRowIdentifierTypedIdComponent')
export class BookmarkTableRowIdentifierTypedIdComponentDo extends BaseDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: string;
}

@typeName('crm.BookmarkTableRowIdentifierStringComponent')
export class BookmarkTableRowIdentifierStringComponentDo extends BaseDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: string;
}

@typeName('crm.BookmarkTableRowIdentifierLongComponent')
export class BookmarkTableRowIdentifierLongComponentDo extends BaseDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: number;
}

// --------------------------------------------------

@typeName(PageIdDummyPageParamDo.TYPE_NAME)
export class PageIdDummyPageParamDo extends BaseDoEntity implements PageParamDo {
  static TYPE_NAME = 'crm.PageIdDummyPageParam';

  pageId: string;
}

// --------------------------------------------------

export class ActivateBookmarkResultDo extends BaseDoEntity {
  remainingPagePath: IBookmarkPageDo[];
  parentBookmarkPage: IBookmarkPageDo;
}

// --------------------------------------------------

export const bookmarks = {

  // FIXME bsh [js-bookmark] Find a better solution! Fix confusion between _type and objectType
  pageParamsMatch(pageParam1: PageParamDo, pageParam2: PageParamDo) {
    if (!pageParam1 && !pageParam2) {
      return true;
    }
    if (!pageParam1 || !pageParam2) {
      return false;
    }

    if (objects.equalsRecursive(pageParam1, pageParam2)) {
      return true;
    }

    let normalizedJson1 = bookmarks.stringifyNormalized(pageParam1);
    let normalizedJson2 = bookmarks.stringifyNormalized(pageParam2);
    return normalizedJson1 === normalizedJson2;
  },

  // FIXME bsh [js-bookmark] move compare logic to dataobjects.ts
  stringifyNormalized(object: any): string {
    return JSON.stringify(object, (key, value) => {
      if (objects.isPlainObject(value)) {
        if (value.objectType) {
          let json = Object.assign({}, value); // shallow copy to keep original object intact
          json._type = DoRegistry.get().toJsonType(value.objectType);
          delete json.objectType;
          value = json;
        } else if (value._type) {
          let json = Object.assign({}, value); // shallow copy to keep original object intact
          delete json._typeVersion; // always ignore type version
          delete json._contributions; // always ignore contributions
          value = json;
        }
        return Object.keys(value)
          .sort()
          .reduce((acc, cur) => {
            acc[cur] = value[cur];
            return acc;
          }, {});
      }
      return value;
    });
  },

  toTypedJson(object: any): any {
    const replacer = (key, value) => {
      if (objects.isPlainObject(value) && value.objectType) {
        let json = Object.assign({}, value); // shallow copy to keep original object intact
        json._type = DoRegistry.get().toJsonType(value.objectType);
        delete json.objectType;
        return json;
      }
      return value;
    };

    return JSON.parse(JSON.stringify(object, replacer));
  },

  toObjectModel(object: any): any {
    const replacer = (key, value) => {
      if (objects.isPlainObject(value) && value._type) {
        let model = Object.assign({}, value); // shallow copy to keep original object intact
        model.objectType = model.objectType || scout.nvl(DoRegistry.get().toObjectType(value._type), 'BaseDoEntity');
        delete model._type;
        delete model._typeVersion;
        return model;
      }
      return value;
    };

    return JSON.parse(JSON.stringify(object, replacer));
  }
} as const;
