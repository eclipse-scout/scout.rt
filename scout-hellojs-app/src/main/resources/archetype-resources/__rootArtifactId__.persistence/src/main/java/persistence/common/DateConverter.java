#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.persistence.common;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import org.jooq.Converter;

public class DateConverter implements Converter<LocalDateTime, Date> {

  private static final long serialVersionUID = 1L;

  @Override
  public Date from(LocalDateTime databaseObject) {
    return Optional.ofNullable(databaseObject).map(dod ->  Date.from(dod.atZone(ZoneId.systemDefault()).toInstant())).orElse(null);
  }

  @Override
  public LocalDateTime to(Date userObject) {
    return Optional.ofNullable(userObject).map(ud -> LocalDateTime.ofInstant(ud.toInstant(), ZoneId.systemDefault())).orElse(null);
  }

  @Override
  public Class<LocalDateTime> fromType() {
    return LocalDateTime.class;
  }

  @Override
  public Class<Date> toType() {
    return Date.class;
  }
}
