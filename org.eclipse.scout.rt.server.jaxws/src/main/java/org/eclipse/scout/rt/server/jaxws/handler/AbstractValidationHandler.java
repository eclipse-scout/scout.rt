package org.eclipse.scout.rt.server.jaxws.handler;

import java.io.IOException;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.XmlUtility;
import org.eclipse.scout.rt.server.jaxws.MessageContexts;
import org.xml.sax.SAXException;

/**
 * <h3>{@link AbstractValidationHandler}</h3><br>
 * Abstract implementation of a {@link Handler} that validates incoming messages against a configured XML Schema
 * (XSD).<br>
 * This may be useful for WebService providers that want to ensure that only valid requests are processed in case there
 * are data restrictions defined in the XSD.<br>
 * To use this class create a sub class and provide the location of the XSD on the classpath using
 * {@link #getSchemaPath()}. Afterwards register the created handler in the chain of the WebService
 * EntryPointDefinition.<br>
 * Note that activating this handler has some performance impact as every request is validated against the XSD first.
 */
@ApplicationScoped
public abstract class AbstractValidationHandler implements LogicalHandler<LogicalMessageContext> {

  private volatile Schema m_schema;

  @Override
  public boolean handleMessage(LogicalMessageContext context) {
    if (MessageContexts.isOutboundMessage(context)) {
      return true;
    }

    LogicalMessage message = context.getMessage();
    Source payload = message.getPayload();
    Schema xsd = getXsd();
    try {
      xsd.newValidator().validate(payload);
    }
    catch (SAXException e) {
      throw new WebServiceException("Soap request message is not valid.", e);
    }
    catch (IOException e) {
      throw new WebServiceException(e);
    }
    return true;
  }

  @Override
  public boolean handleFault(LogicalMessageContext context) {
    return true; // do not parse error responses
  }

  @Override
  public void close(MessageContext context) {
    // nop
  }

  protected Schema getXsd() {
    Schema result = m_schema;
    if (result != null) {
      return result;
    }
    synchronized (this) {
      result = m_schema;
      if (result != null) {
        return result;
      }
      result = loadXsd();
      m_schema = result;
      return result;
    }
  }

  protected Schema loadXsd() {
    String path = getSchemaPath();
    if (!StringUtility.hasText(path)) {
      throw new IllegalArgumentException("No Schema path specified.");
    }

    URL xsdUrl = AbstractValidationHandler.class.getClassLoader().getResource(path);
    if (xsdUrl == null) {
      throw new IllegalArgumentException("Unable to read SOAP request validation schema. The following resource could not be found on the classpath: " + path);
    }
    try {
      return XmlUtility.newSchemaFactory().newSchema(xsdUrl);
    }
    catch (SAXException e) {
      throw new IllegalArgumentException("Unable to read SOAP request validation schema.", e);
    }
  }

  /**
   * @return Gets the location on the classpath where the XSD root file can be found. (e.g.
   *         <code>WEB-INF/wsdl/MyWebService/schema.xsd</code>).
   */
  protected abstract String getSchemaPath();
}
