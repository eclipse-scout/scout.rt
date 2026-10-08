import {BaseDoEntity, typeName} from '@eclipse-scout/core';

@typeName()
export abstract class AbstractItemResponse<TItem extends BaseDoEntity> extends BaseDoEntity {
  items: TItem[];
}
