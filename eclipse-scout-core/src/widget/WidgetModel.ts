import {Session, Widget} from '../index';

export default interface WidgetModel {
    parent: Widget,
    owner?: Widget,
    session?: Session,
    objectType?: string
};
