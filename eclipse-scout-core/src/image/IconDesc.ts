/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {EnumObject, icons} from '../index';

export type IconType = EnumObject<typeof IconDesc.IconType>;

export class IconDesc {
  iconType: IconType;
  font: string;
  iconCharacter: string;
  iconUrl: string;

  constructor() {
    this.iconType = null;
    this.font = null;
    this.iconCharacter = null;
    this.iconUrl = null;
  }

  static IconType = {
    FONT_ICON: 0,
    BITMAP: 1
  } as const;

  static DEFAULT_FONT = 'scoutIcons';

  /**
   * Returns a CSS class based on the used font-name.
   */
  cssClass(): string {
    if (this.isFontIcon() && this.font !== IconDesc.DEFAULT_FONT) {
      return 'font-' + this.font;
    }
    return '';
  }

  /**
   * Returns a CSS class string to be used with JQuery.add/removeClass().
   */
  appendCssClass(cssClass: string): string {
    let additionalCssClass = this.cssClass();
    if (additionalCssClass.length > 0) {
      return cssClass + ' ' + additionalCssClass;
    }
    return cssClass;
  }

  isFontIcon(): boolean {
    return this.iconType === IconDesc.IconType.FONT_ICON;
  }

  isBitmap(): boolean {
    return this.iconType === IconDesc.IconType.BITMAP;
  }

  /**
   * Ensures that the given icon is of type {@link IconDesc}. If a string is provided, it is assumed that the string is the iconId which may be parsed to create the {@link IconDesc}.
   */
  static ensure(icon: string | IconDesc): IconDesc {
    if (!icon) {
      return icon as IconDesc;
    }
    if (icon instanceof IconDesc) {
      return icon;
    }
    return icons.parseIconId(icon);
  }
}
