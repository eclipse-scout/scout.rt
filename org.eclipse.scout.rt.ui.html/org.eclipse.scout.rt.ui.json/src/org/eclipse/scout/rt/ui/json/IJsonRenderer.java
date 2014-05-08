package org.eclipse.scout.rt.ui.json;

import org.json.JSONObject;

/**
 * Creates JSON output for a Scout model object.
 * 
 * @param <T>
 *            Type of Scout model
 */
public interface IJsonRenderer<T extends Object> {

	String getId();

	/**
	 * Returns a string used to identify the object-type in the JSON output
	 * (JSON attribute 'objectType').
	 * 
	 * @return
	 */
	String getObjectType();

	/**
	 * Returns the Scout model object.
	 * 
	 * @return
	 */
	T getModelObject();

	void init() throws JsonException;

	void dispose() throws JsonException;

	JSONObject toJson() throws JsonException;

	void handleUiEvent(JsonEvent event, JsonResponse res) throws JsonException;

}
