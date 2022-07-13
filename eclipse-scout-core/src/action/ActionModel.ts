import WidgetModel from '../widget/WidgetModel';
import {ActionStyle} from './Action';

export interface ActionModel extends WidgetModel {
  actionStyle?: ActionStyle;
  iconId?: string;
  text?: string;
  showTooltipWhenSelected?: boolean;
}
