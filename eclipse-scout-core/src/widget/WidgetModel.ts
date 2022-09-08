import {DisabledStyle, LogicalGrid, Session, Widget} from '../index';
import {PartialAndRequired} from '../types';
import {ObjectType} from '../ObjectFactory';

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
  objectType?: ObjectType<Widget>;
  enabled?: boolean;
  trackFocus?: boolean;
  scrollTop?: number;
  scrollLeft?: number;
  inheritAccessibility?: boolean;
  disabledStyle?: DisabledStyle;
  visible?: boolean;
  cssClass?: string;
  loading?: boolean;
  logicalGrid?: LogicalGrid;

  [property: string]: any; // FIXME TS necessary for variable model properties, required?
}

export type RefWidgetModel<T extends WidgetModel> = PartialAndRequired<T, 'parent', 'objectType'>;
