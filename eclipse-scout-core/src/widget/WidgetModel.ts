import {Session, Widget} from '../index';

export default interface WidgetModel {
  /**
   * The parent widget.
   */
  parent: Widget,
  owner?: Widget,
  /**
   * If not specified, the session of the parent widget is used
   */
  session?: Session,
  objectType?: string,
  [property: string]: any // TODO CGU necessary for variable model properties, required?
}
