import {Widget} from '../index';

export default interface WidgetSupportOptions {
  /**
   * Widget that created the support
   */
  widget: Widget;

  /**
   * JQuery element that will be used for the visualization.
   * It may be a function to resolve the container later.
   * If this property is not set the $container of the widget is used by default.
   */
  $container?: JQuery | (() => JQuery);
}
