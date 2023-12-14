/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.multipart;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import jakarta.ws.rs.core.MediaType;

import org.eclipse.scout.rt.rest.IRestResource;

/**
 * Multipart message for receiving a REST service invocation using a multipart request, i.e. within a
 * {@link IRestResource}.
 * <p>
 * The input stream of each part returned by the iterator must be consumed exactly once. The {@link AutoCloseable} will
 * consume it before processing the next part if it wasn't fully consumed yet.
 * <p>
 * Only supports {@link StandardCharsets#UTF_8} encoding.
 * <p>
 * Example:
 *
 * <pre>
 * &#064;POST
 * &#064;Path("upload")
 * &#064;Consumes(MediaType.MULTIPART_FORM_DATA)
 * &#064;Produces(MediaType.APPLICATION_JSON)
 * public UploadResponseDo upload(@HeaderParam(&quot;Content-Type&quot;) MediaType mediaType, InputStream inputStream) {
 *   ...
 *   IMultipartMessage multipartMessage = IMultipartMessage.of(mediaType, inputStream);
 *   while (multipartMessage.hasNext()) {
 *     try (IMultipartPart part = multipartMessage.next()) {
 *       switch (part.getPartName()) {
 *         case "definition": // file field part
 *           definition = BEANS.get(IDataObjectMapper.class).readValue(part.getInputStream(), DefinitionDo.class);
 *           definitionFilename = part.getFilename();
 *           break;
 *         case "displayText": // text field part
 *           displayText = IOUtility.readStringUTF8(part.getInputStream());
 *           break;
 *         default:
 *           throw new VetoException("Unexpected part {}", part.getPartName());
 *       }
 *     }
 *     catch (Exception e) {
 *       throw new PlatformException("Failed to handle multipart", e);
 *     }
 *   }
 *
 *   ...
 *
 *   return BEANS.get(UploadResponseDo.class)
 *     .with(...);
 * }
 * </pre>
 */
public interface IMultipartMessage extends Iterator<IMultipartPart> {

  /**
   * Manually creates a {@link IMultipartMessage} based on media type and input stream.
   */
  static IMultipartMessage of(MediaType mediaType, InputStream inputStream) {
    return new ServerMultipartMessage(mediaType, inputStream);
  }
}
