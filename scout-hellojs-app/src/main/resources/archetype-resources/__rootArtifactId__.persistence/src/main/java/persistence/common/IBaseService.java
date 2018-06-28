#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.persistence.common;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

@ApplicationScoped
public interface IBaseService<TABLE extends Table<RECORD>, RECORD extends Record, DO extends DoEntity> {

  /**
   * Returns the table object associated with this service.
   */
  TABLE getTable();

  /**
   * Returns the id column for the table object associated with this service.
   */
  Field<String> getIdColumn();

  /**
   * Deletes the record with the specified id Returns number of records deleted
   */
  int remove(String id);

  /**
   * Returns the record for the specified id. Returns null if no such record exists.
   */
  Optional<RECORD> get(String id);

  /**
   * Returns all available records.
   */
  Stream<RECORD> getAll();

  /**
   * Persists the provided record based on the id specified. If no record with this id exists a new record is created,
   * otherwise the existing record is updated.
   */
  void store(String id, RECORD record);

  /**
   * @return
   */
  RECORD newRecord();

}
