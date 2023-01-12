/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays} from '../index';

export const files = {
  /**
   * Checks if the combined total size of the given files does not exceed the maximum upload size.
   *
   * @param files array of files
   * @param maximumUploadSize maximum combined file size. null disables the size check
   * @returns true if the total size does not exceed maximumUploadSize, otherwise false
   */
  validateMaximumUploadSize(files: Blob | Blob[], maximumUploadSize: number): boolean {
    files = arrays.ensure(files);
    if (files.length === 0 || maximumUploadSize == null) {
      return true;
    }

    let totalSize = files.reduce((total, file) => total + file.size, 0);
    return totalSize <= maximumUploadSize;
  },

  fileListToArray(fileList: FileList): File[] {
    let files = [];
    for (let i = 0; i < fileList.length; i++) {
      files.push(fileList[i]);
    }
    return files;
  }
};
