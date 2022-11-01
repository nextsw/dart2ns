package rest.ws;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import classes.MutateResultStatus;
import d3e.core.CurrentUser;
import d3e.core.ExternalSystem;
import d3e.core.ListExt;
import gqltosql.schema.DModel;
import gqltosql.schema.IModelSchema;
import models.CreatableObject;
import models.BaseUser;
import store.EntityMutator;
import store.ValidationFailedException;

@Service
public class RocketMutation {

	@Autowired
	private EntityMutator mutator;
	
	@Autowired(required =  false)
	private Map<String, ExternalSystem> externalSystems;
	
	@Autowired
	private IModelSchema schema;

	public void save(CreatableObject obj) throws Exception {
		boolean create = obj.isNew();
		BaseUser currentUser = CurrentUser.get();
		int objType = obj._typeIdx();

		if (create && !PermissionCheckUtil.canCreate(currentUser._typeIdx(), obj._typeIdx())) {
			throw authFail("Current user type does not have create permissions for this model.");
		}
		if (!create && !PermissionCheckUtil.canUpdate(currentUser._typeIdx(), obj._typeIdx())) {
			throw authFail("Current user type does not have create permissions for this model.");
		}
		DModel<?> dModel = schema.getType(objType);
		if (!dModel.isExternal()) {
			this.mutator.save(obj, false);
		} else {
			ExternalSystem externalSystem = this.externalSystems.get(dModel.getExternal());
			externalSystem.save(obj, false);
		}
	}

	private ValidationFailedException authFail(String msg) {
		return new ValidationFailedException(MutateResultStatus.AuthFail, ListExt.asList(msg));
	}

	public void delete(CreatableObject obj, boolean internal) throws Exception {
		BaseUser currentUser = CurrentUser.get();
		int objType = obj._typeIdx();

		if (!PermissionCheckUtil.canDelete(currentUser._typeIdx(), obj._typeIdx())) {
			throw authFail("Current user type does not have delete permissions for this model.");
		}
		DModel<?> dModel = schema.getType(objType);
		if (!dModel.isExternal()) {
			this.mutator.delete(obj, false);
		} else {
			ExternalSystem externalSystem = this.externalSystems.get(dModel.getExternal());
			externalSystem.delete(obj, false);
		}
	}

}
