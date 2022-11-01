package rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import d3e.core.CurrentUser;
import d3e.core.RestControllerBase;
import gqltosql.schema.DModel;
import gqltosql.schema.IModelSchema;
import models.BaseUser;
import rest.ws.APIDataQueryUtil;
import rest.ws.PermissionCheckUtil;
import store.D3EEntityManagerProvider;
import store.DBObject;
import store.Database;
import store.ValidationFailedException;

@RestController
public class APIController extends RestControllerBase {
	@Autowired private IModelSchema schema;
	@Autowired private D3EEntityManagerProvider provider;
	
	@GetMapping("/api/data/{type}/{id}")
	public void getById(@PathVariable String type, @PathVariable long id) {
		// Check permission
		if (!hasReadPermission(type)) {
			markForbidden();
			return;
		}
		
		// Get the Model by id
		DModel<?> model = schema.getType(type);
		if (model == null) {
			markNotFound();
			return;
		}
		
		// Only creatable models can be accessed by id
		if (!model.isCreatable()) {
			markForbidden();
			return;
		}
		
		Object byId = provider.get().getById(model.getIndex(), id);
		if (byId == null) {
			markNotFound();
			return;
		}
		
		// Write the object to response in JSON
		writeObjectAsJson(type, byId);
	}
	
	@PostMapping("/api/data/{type}")
	public void create(@PathVariable String type, @RequestBody String toSave) {
		if (type == null || toSave == null) {
			return;
		}
		
		// Get Model
		DModel<?> model = schema.getType(type);
		if (model == null) {
			markNotFound();
			return;
		}
		
		// Check permission
		if (!hasCreatePermission(model.getIndex())) {
			markForbidden();
			return;
		}
		
		// Read from json and save
		DBObject object = convertFromJson(toSave, type);
		if (object == null) {
			markServerError();
			return;
		}
		try {
			Database.get().save(object);
			writeObjectAsJson(type, object);
		} catch (ValidationFailedException e) {
			markBadRequest();
		}
	}
	
	@PostMapping("/api/data/{type}/{id}")
	public void update(@PathVariable String type, @PathVariable long id, @RequestBody String toUpdate) {
		if (type == null || toUpdate == null) {
			return;
		}
		
		// Get Model
		DModel<?> model = schema.getType(type);
		if (model == null) {
			markNotFound();
			return;
		}
		
		// Check permission
		if (!hasUpdatePermission(model.getIndex())) {
			markForbidden();
			return;
		}
		
		Object byId = provider.get().getById(model.getIndex(), id);
		if (byId == null) {
			// If object does not already exist
			markNotFound();
			return;
		}
		
		// Read from json and update
		updateObjectFromJson((DBObject) byId, toUpdate, type);
		try {
			Database.get().update(byId);
			writeObjectAsJson(type, byId);
		} catch (ValidationFailedException e) {
			markBadRequest();
		}
	}
	
	@DeleteMapping("/api/data/{type}/{id}")
	public void delete(@PathVariable String type, @PathVariable long id) {
		if (type == null) {
			return;
		}
		
		// Get Model
		DModel<?> model = schema.getType(type);
		if (model == null) {
			markNotFound();
			return;
		}
		
		// Check permission
		if (!hasDeletePermission(model.getIndex())) {
			markForbidden();
			return;
		}
		
		Object byId = provider.get().getById(model.getIndex(), id);
		if (byId == null) {
			// If object does not already exist
			markNotFound();
			return;
		}
		
		// Delete
		try {
			Database.get().delete(byId);
		} catch (ValidationFailedException e) {
			markBadRequest();
		}
	}
	
	@PostMapping("/api/query/{dq}")
	public void query(@PathVariable String dq, @RequestBody(required = false) String inputs) {
		DModel<?> type = schema.getType(dq);
		if (type == null) {
			markNotFound();
			return;
		}
		
		Object input = null;
		if (inputs != null) {
			input = convertFromJson(inputs, dq + "Request");
			if (input == null) {
				markBadRequest();
				return;
			}
		}
		
		BaseUser user = CurrentUser.get();
		int dqTypeIdx = type.getIndex();
		if (user == null || !PermissionCheckUtil.canReadDataQuery(user._typeIdx(), dqTypeIdx)) {
			markForbidden();
			return;
		}
		try {
			DBObject result = APIDataQueryUtil.get(dqTypeIdx, input);
			if (result == null) {
				markNotFound();
				return;
			}
			writeObjectAsJson(dq, result);
		} catch (NullPointerException e) {
			// If DQ has inputs, but none were given in request, then NPE will be thrown by Impl class
			e.printStackTrace();
			markBadRequest();
		}
	}
	
	private boolean hasReadPermission(String type) {
		// TODO
		return true;
	}
	
	private boolean hasCreatePermission(int type) {
		BaseUser user = CurrentUser.get();
		if (user == null) {
			return false;
		}
		return PermissionCheckUtil.canCreate(user._typeIdx(), type);
	}
	
	private boolean hasUpdatePermission(int type) {
		BaseUser user = CurrentUser.get();
		if (user == null) {
			return false;
		}
		return PermissionCheckUtil.canUpdate(user._typeIdx(), type);
	}
	
	private boolean hasDeletePermission(int type) {
		BaseUser user = CurrentUser.get();
		if (user == null) {
			return false;
		}
		return PermissionCheckUtil.canDelete(user._typeIdx(), type);
	}
}
