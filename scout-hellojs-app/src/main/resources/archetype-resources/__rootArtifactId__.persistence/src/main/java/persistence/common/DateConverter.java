#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.persistence.common;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;

import org.jooq.Converter;

public class DateConverter implements Converter<Timestamp, Date> {

  private static final long serialVersionUID = 1L;

  @Override
  public Date from(Timestamp databaseObject) {
    return Optional.ofNullable(databaseObject).map(dod -> new Date(dod.getTime())).orElse(null);
  }

  @Override
  public Timestamp to(Date userObject) {
    return Optional.ofNullable(userObject).map(ud -> new Timestamp(ud.getTime())).orElse(null);
  }

  @Override
  public Class<Timestamp> fromType() {
    return Timestamp.class;
  }

  @Override
  public Class<Date> toType() {
    return Date.class;
  }
}
