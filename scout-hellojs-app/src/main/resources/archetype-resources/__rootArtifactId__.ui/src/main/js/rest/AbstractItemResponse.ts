import {BaseDoEntity} from '@eclipse-scout/core';

export abstract class AbstractItemResponse<TItem extends BaseDoEntity> extends BaseDoEntity {
  items: TItem[];
}
