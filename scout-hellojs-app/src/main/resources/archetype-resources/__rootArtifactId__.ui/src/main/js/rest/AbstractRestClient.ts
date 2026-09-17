import {ajax, AjaxError, BaseDoEntity, ObjectWithType, scout} from '@eclipse-scout/core';
import {AbstractItemResponse} from '../index';

export abstract class AbstractRestClient implements ObjectWithType {

  objectType: string;
  dataType: string;
  targetUrl: string;

  protected constructor(dataType: string, targetUrl: string) {
    this.dataType = dataType;
    this.targetUrl = targetUrl;
  }

  protected _loadItem<TItem extends BaseDoEntity>(id: string): Promise<TItem> {
    return ajax.getDataObject(this.targetUrl + id)
      .then((res: AbstractItemResponse<TItem>) => res.items[0]);
  }

  protected _listItems<TItem extends BaseDoEntity>(restriction: BaseDoEntity): Promise<TItem[]> {
    return ajax.postDataObject(this.targetUrl + 'list', restriction)
      .then((res: AbstractItemResponse<TItem>) => res.items);
  }

  protected _createItem<TItem extends BaseDoEntity>(data: BaseDoEntity): Promise<TItem> {
    return ajax.postDataObject(this.targetUrl, data)
      .then((res: AbstractItemResponse<TItem>) => this._triggerDataChange(res.items[0]));
  }

  protected _storeItem<TItem extends BaseDoEntity>(id: string, data: BaseDoEntity): Promise<TItem> {
    return ajax.putDataObject(this.targetUrl + id, data)
      .then((res: AbstractItemResponse<TItem>) => this._triggerDataChange(res.items[0]));
  }

  protected _removeItem(id: string): Promise<void> {
    return ajax.removeDataObject(this.targetUrl + id)
      .then(() => this._triggerDataChange());
  }

  protected _triggerDataChange<TData>(data?: TData): TData {
    scout.getSession().desktop.dataChange({
      dataType: this.dataType,
      data
    });
    return data;
  }
}
