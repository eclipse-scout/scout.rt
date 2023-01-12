/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, Dimension, HtmlCompPrefSizeOptions, Image} from '../index';

export class ImageLayout extends AbstractLayout {
  image: Image;

  constructor(image: Image) {
    super();
    this.image = image;
  }

  override preferredLayoutSize($container: JQuery<HTMLImageElement>, options?: HtmlCompPrefSizeOptions): Dimension {
    let img = $container[0];
    if (img && img.complete && img.naturalWidth > 0 && img.naturalHeight > 0) {
      let prefHeight = img.naturalHeight;
      let prefWidth = img.naturalWidth;
      if (options.widthHint > 0 && options.widthHint < img.naturalWidth) {
        prefHeight = options.widthHint / img.naturalWidth * img.naturalHeight;
        prefWidth = options.widthHint;
      } else if (options.heightHint > 0 && options.heightHint < img.naturalHeight) {
        prefHeight = options.heightHint;
        prefWidth = options.heightHint / img.naturalHeight * img.naturalWidth;
      }
      return new Dimension(prefWidth, prefHeight);
    }
    return super.preferredLayoutSize($container, options);
  }
}
