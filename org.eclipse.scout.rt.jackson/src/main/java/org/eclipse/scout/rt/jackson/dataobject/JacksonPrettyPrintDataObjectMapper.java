package org.eclipse.scout.rt.jackson.dataobject;

import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.platform.dataobject.IPrettyPrintDataObjectMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * {@link IDataObjectMapper} implementation based on jackson {@link ObjectMapper} with output indentation enabled (e.g.
 * pretty formatted JSON output).
 */
@Order(IBean.DEFAULT_BEAN_ORDER + 100)
public class JacksonPrettyPrintDataObjectMapper extends JacksonDataObjectMapper implements IPrettyPrintDataObjectMapper {

  @Override
  protected ObjectMapper createObjectMapperInstance() {
    ObjectMapper om = super.createObjectMapperInstance();
    om.enable(SerializationFeature.INDENT_OUTPUT);
    return om;
  }
}
