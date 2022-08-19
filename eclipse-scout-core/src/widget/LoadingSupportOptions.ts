import {WidgetSupportOptions} from '../index';

export default interface LoadingSupportOptions extends WidgetSupportOptions {

  /**
   * If not set: 250 ms
   */
  loadingIndicatorDelay?: number;
}
