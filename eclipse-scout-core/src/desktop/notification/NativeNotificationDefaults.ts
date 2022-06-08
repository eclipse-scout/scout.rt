import {DesktopNotification} from '../../index';

export default interface NativeNotificationDefaults {
title: string,
    iconId: string,
    visibility: any // TODO How to access NativeNotificationVisibility?
}