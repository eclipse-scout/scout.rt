import {Session, Widget} from '../index';

export default interface WidgetModel {
  /**
   * The parent widget.
   */
  parent: Widget;
  owner?: Widget;
  /**
   * If not specified, the session of the parent widget is used
   */
  session?: Session;
  objectType?: string | { new(): Widget };

  [property: string]: any; // TODO CGU necessary for variable model properties, required?
}

// Type that makes some properties optional and some required. TODO CGU move to separate file in util
export type PartialAndRequired<T, OPTIONAL extends keyof T, REQUIRED extends keyof T> = Omit<T, OPTIONAL | REQUIRED> & Partial<Pick<T, OPTIONAL>> & Required<Pick<T, REQUIRED>>;

export type RefWidgetModel<T extends WidgetModel> = PartialAndRequired<T, 'parent', 'objectType'>;
