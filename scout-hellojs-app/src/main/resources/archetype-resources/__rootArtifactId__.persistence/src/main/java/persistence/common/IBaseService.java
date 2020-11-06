#set($symbol_pound='#')#set($symbol_dollar='$')#set($symbol_escape='\')
package ${package}.persistence.common;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

@ApplicationScoped
public interface IBaseService<TABLE extends Table<RECORD>, RECORD extends Record, DO extends DoEntity> {

  /**
   * @return the table object associated with this service.
   */
  TABLE getTable();

  /**
   * @return the id column for the table object associated with this service.
   */
  Field<String> getIdColumn();

  /**
   * Deletes the record with the specified id
   * 
   * @return the number of records deleted
   */
  int remove(String id);

  /**
   * Gets the record for the specified id.
   * 
   * @return the record for the id given or an empty {@link Optional} if the id could not be found.
   */
  Optional<RECORD> get(String id);

  /**
   * @return all available records.
   */
  Stream<RECORD> getAll();

  /**
   * Persists the provided record based on the id specified. If no record with this id exists, a new record is created.
   * Otherwise the existing record is updated.
   */
  void store(String id, RECORD record);

  /**
   * @return A new empty record.
   */
  RECORD newRecord();

}
