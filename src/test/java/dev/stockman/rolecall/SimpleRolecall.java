package dev.stockman.rolecall;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SimpleRolecall extends Rolecall<PermissionSet> {

    private final Function<String, PermissionSet> permissionSetFunction;

    public SimpleRolecall(Function<String, PermissionSet> permissionSetFunction) {
        this.permissionSetFunction = permissionSetFunction;
    }

    @Override
    protected PermissionSet[] fetchPermissionSets(String... roles) {
        List<PermissionSet> sets = new ArrayList<>();
        for (String role : roles) {
            PermissionSet set = permissionSetFunction.apply(role);
            if (set != null) {
                sets.add(set);
            }
        }
        return sets.toArray(new PermissionSet[0]);
    }

    @Override
    protected PermissionSet merge(PermissionSet first, PermissionSet second) {
        return new PermissionSet(first.edit() || second.edit(), first.publish() || second.publish());
    }

    @Override
    protected PermissionSet defaultPermissionSet() {
        return new PermissionSet(false, false);
    }
}
