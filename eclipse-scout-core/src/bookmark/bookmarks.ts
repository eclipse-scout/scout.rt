/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, DoEntity, ObjectFactory, objects, ObjectType, ObjectWithType, PageParamDo} from '../index';

export abstract class AbstractDoEntity implements ObjectWithType, DoEntity {
  _type?: string; // FIXME bsh Better solution?
  objectType: string;
}

export class BookmarkDo extends AbstractDoEntity {
  key: string;
  titles: Record<string, string>;
  description: string;
  definition: IBookmarkDefinitionDo;

  constructor(model: Partial<BookmarkDo>) {
    super();
    Object.assign(this, model);
  }
}

// --------------------------------------------------

export interface IBookmarkDefinitionDo {
  bookmarkedPage: IBookmarkPageDo;
}

export class OutlineBookmarkDefinitionDo extends AbstractDoEntity implements IBookmarkDefinitionDo {
  bookmarkedPage: IBookmarkPageDo;
  outlineId: string;
  /** Path from the outline's root to the {@link bookmarkedPage} */
  pagePath: IBookmarkPageDo[];

  constructor(model: Partial<OutlineBookmarkDefinitionDo>) {
    super();
    Object.assign(this, model);
  }
}

export class PageBookmarkDefinitionDo extends AbstractDoEntity implements IBookmarkDefinitionDo {
  bookmarkedPage: IBookmarkPageDo;

  constructor(model: Partial<PageBookmarkDefinitionDo>) {
    super();
    Object.assign(this, model);
  }
}

// --------------------------------------------------

export interface IBookmarkPageDo {
  pageParam: PageParamDo;
  displayText: string;
}

export class NodeBookmarkPageDo extends AbstractDoEntity implements IBookmarkPageDo {
  pageParam: PageParamDo;
  displayText: string;

  constructor(model: Partial<NodeBookmarkPageDo>) {
    super();
    Object.assign(this, model);
  }
}

export class TableBookmarkPageDo extends AbstractDoEntity implements IBookmarkPageDo {
  pageParam: PageParamDo;
  displayText: string;
  expandedChildRow: BookmarkTableRowIdentifierDo;
  selectedChildRows: BookmarkTableRowIdentifierDo[];
  // tablePreferences: TableClientUiPreferencesDo;
  // searchFilterComplete: boolean;
  // searchData: ISearchDo;
  // chartTableControlConfig: ChartTableControlConfigDo;

  constructor(model: Partial<TableBookmarkPageDo>) {
    super();
    Object.assign(this, model);
  }
}

// --------------------------------------------------

export class BookmarkTableRowIdentifierDo extends AbstractDoEntity {
  keyComponents: IBookmarkTableRowIdentifierComponentDo[];

  constructor(model: Partial<BookmarkTableRowIdentifierDo>) {
    super();
    Object.assign(this, model);
  }
}

export interface IBookmarkTableRowIdentifierComponentDo {
}

/**
 * Never serialize this!
 */
export class BookmarkTableRowIdentifierObjectComponentDo extends AbstractDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: any;

  constructor(model: Partial<BookmarkTableRowIdentifierObjectComponentDo>) {
    super();
    Object.assign(this, model);
  }
}

export class BookmarkTableRowIdentifierEntityKeyComponentDo extends AbstractDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: string;

  constructor(model: Partial<BookmarkTableRowIdentifierEntityKeyComponentDo>) {
    super();
    Object.assign(this, model);
  }
}

export class BookmarkTableRowIdentifierDateComponentDo extends AbstractDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: string;

  constructor(model: Partial<BookmarkTableRowIdentifierDateComponentDo>) {
    super();
    Object.assign(this, model);
  }
}

export class BookmarkTableRowIdentifierBooleanComponentDo extends AbstractDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: boolean;

  constructor(model: Partial<BookmarkTableRowIdentifierBooleanComponentDo>) {
    super();
    Object.assign(this, model);
  }
}

export class BookmarkTableRowIdentifierIntegerComponentDo extends AbstractDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: number;

  constructor(model: Partial<BookmarkTableRowIdentifierIntegerComponentDo>) {
    super();
    Object.assign(this, model);
  }
}

export class BookmarkTableRowIdentifierTypedIdComponentDo extends AbstractDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: string;

  constructor(model: Partial<BookmarkTableRowIdentifierTypedIdComponentDo>) {
    super();
    Object.assign(this, model);
  }
}

export class BookmarkTableRowIdentifierStringComponentDo extends AbstractDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: string;

  constructor(model: Partial<BookmarkTableRowIdentifierStringComponentDo>) {
    super();
    Object.assign(this, model);
  }
}

export class BookmarkTableRowIdentifierLongComponentDo extends AbstractDoEntity implements IBookmarkTableRowIdentifierComponentDo {
  key: number;

  constructor(model: Partial<BookmarkTableRowIdentifierLongComponentDo>) {
    super();
    Object.assign(this, model);
  }
}

// --------------------------------------------------

export class PageIdDummyPageParamDo extends AbstractDoEntity implements PageParamDo {
  pageId: string;

  constructor(model: Partial<PageIdDummyPageParamDo>) {
    super();
    Object.assign(this, model);
  }
}

// --------------------------------------------------

let objectTypeToJsonTypeMap = {};
let jsonTypeToObjectTypeMap = {};
export const ObjectTypeToJsonTypeMapper = {

  register(objectType: ObjectType, jsonType: string) {
    objectType = ObjectFactory.get().getObjectType(objectType);
    objectTypeToJsonTypeMap[objectType] = jsonType;
    jsonTypeToObjectTypeMap[jsonType] = objectType;
  },

  toJsonType(objectType: ObjectType): string {
    objectType = ObjectFactory.get().getObjectType(objectType);
    let jsonType = objectTypeToJsonTypeMap[objectType];
    if (!jsonType) {
      throw new Error('No mapping found for object type "' + objectType + '"');
    }
    return jsonType;
  },

  toObjectType(jsonType: string): string {
    let objectType = ObjectTypeToJsonTypeMapper.optObjectType(jsonType);
    if (!objectType) {
      throw new Error('No mapping found for json type "' + jsonType + '"');
    }
    return objectType;
  },

  optObjectType(jsonType: string): string {
    return jsonTypeToObjectTypeMap[jsonType] || null;
  }
};

App.addListener('bootstrap', () => { // needed to make sure object registry is ready
  ObjectTypeToJsonTypeMapper.register(BookmarkDo, 'crm.Bookmark');
  ObjectTypeToJsonTypeMapper.register(OutlineBookmarkDefinitionDo, 'crm.OutlineBookmarkDefinition');
  ObjectTypeToJsonTypeMapper.register(PageBookmarkDefinitionDo, 'crm.PageBookmarkDefinition');
  ObjectTypeToJsonTypeMapper.register(NodeBookmarkPageDo, 'crm.NodeBookmarkPage');
  ObjectTypeToJsonTypeMapper.register(TableBookmarkPageDo, 'crm.TableBookmarkPage');
  ObjectTypeToJsonTypeMapper.register(BookmarkTableRowIdentifierDo, 'crm.BookmarkTableRowIdentifier');
  ObjectTypeToJsonTypeMapper.register(BookmarkTableRowIdentifierObjectComponentDo, 'crm.BookmarkTableRowIdentifierObjectComponent');
  ObjectTypeToJsonTypeMapper.register(BookmarkTableRowIdentifierEntityKeyComponentDo, 'crm.BookmarkTableRowIdentifierEntityKeyComponent');
  ObjectTypeToJsonTypeMapper.register(BookmarkTableRowIdentifierDateComponentDo, 'crm.BookmarkTableRowIdentifierDateComponent');
  ObjectTypeToJsonTypeMapper.register(BookmarkTableRowIdentifierBooleanComponentDo, 'crm.BookmarkTableRowIdentifierBooleanComponent');
  ObjectTypeToJsonTypeMapper.register(BookmarkTableRowIdentifierIntegerComponentDo, 'crm.BookmarkTableRowIdentifierIntegerComponent');
  ObjectTypeToJsonTypeMapper.register(BookmarkTableRowIdentifierTypedIdComponentDo, 'crm.BookmarkTableRowIdentifierTypedIdComponent');
  ObjectTypeToJsonTypeMapper.register(BookmarkTableRowIdentifierStringComponentDo, 'crm.BookmarkTableRowIdentifierStringComponent');
  ObjectTypeToJsonTypeMapper.register(BookmarkTableRowIdentifierLongComponentDo, 'crm.BookmarkTableRowIdentifierLongComponent');
  ObjectTypeToJsonTypeMapper.register(PageIdDummyPageParamDo, 'crm.PageIdDummyPageParam');
});

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

  // FIXME bsh [js-bookmark] move to objects.ts
  stringifyNormalized(object: any): string {
    return JSON.stringify(object, (key, value) => {
      if (objects.isPlainObject(value)) {
        if (value.objectType) {
          let json = Object.assign({}, value); // shallow copy to keep original object intact
          json._type = ObjectTypeToJsonTypeMapper.toJsonType(value.objectType);
          delete json.objectType;
          value = json;
        } else if (value._type) {
          let json = Object.assign({}, value); // shallow copy to keep original object intact
          delete json._typeVersion; // always ignore type version
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
        json._type = ObjectTypeToJsonTypeMapper.toJsonType(value.objectType);
        delete json.objectType;
        return json;
      }
      return value;
    };

    return JSON.parse(JSON.stringify(object, replacer));
  }
} as const;
