/**
 * Pendant for org.eclipse.scout.commons.resource.MimeType enumeration.
 */
scout.mimeTypes = {

  TEXT_PLAIN: 'text/plain',

  IMAGE_PNG: 'image/png',
  IMAGE_JPG: 'image/jpg',
  IMAGE_JPEG: 'image/jpeg',
  IMAGE_GIF: 'image/gif',

  getDefaultFilename: function(type, suffix, prefix) {
    // TODO MOT/BSH Use translated texts
    // TODO MOT/BSH Check with com.bsiag.crm.client.core.document.AbstractDocumentTableBox.execHandleClipboardPaste(Collection<BinaryResource>)
    var filename = 'Data';
    var extension = '';
    if (type === this.TEXT_PLAIN) {
      filename = 'Text';
      extension = '.txt';
    }
    else if (type === this.IMAGE_PNG) {
      filename = 'Picture';
      extension = '.png';
    }
    else if (type === this.IMAGE_JPG) {
      filename = 'Picture';
      extension = '.jpg';
    }
    else if (type === this.IMAGE_JPEG) {
      filename = 'Picture';
      extension = '.jpeg';
    }
    else if (type === this.IMAGE_GIF) {
      filename = 'Picture';
      extension = '.gif';
    }
    return scout.strings.box(prefix, filename, suffix) + extension;
  }

};
