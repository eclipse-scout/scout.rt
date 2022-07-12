import WidgetModel from '../widget/WidgetModel';
import {IconDesc} from '../index';

export interface IconModel extends WidgetModel {
  autoFit?: boolean
  iconDesc?: IconDesc | string
  prepend?: boolean
}
