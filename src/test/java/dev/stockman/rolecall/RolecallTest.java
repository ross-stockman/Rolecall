package dev.stockman.rolecall;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

public class RolecallTest {

    private static final Map<String, PermissionSet> STATIC_PERMISSION_SETS = Map.of(
            "Visitor", new PermissionSet(false, false),
            "User", new PermissionSet(true, true),
            "Admin", new PermissionSet(true, false),
            "Moderator", new PermissionSet(false, true)
    );

    @ParameterizedTest(name = "roles=[\"{0}\", \"{1}\"]")
    @CsvSource({
            "Visitor, Visitor, false, false",
            "Visitor, User, true, true",
            "Visitor, Admin, true, false",
            "Visitor, Moderator, false, true",
            "User, Visitor, true, true",
            "User, User, true, true",
            "User, Admin, true, true",
            "User, Moderator, true, true",
            "Admin, Visitor, true, false",
            "Admin, User, true, true",
            "Admin, Admin, true, false",
            "Admin, Moderator, true, true",
            "Moderator, Visitor, false, true",
            "Moderator, User, true, true",
            "Moderator, Admin, true, true",
            "Moderator, Moderator, false, true"
    })
    @DisplayName("Merge all combinations")
    void testMergeAllCombinations(String role1, String role2, boolean expectedEdit, boolean expectedPublish) {
        Rolecall<PermissionSet> rolecall = new SimpleRolecall(STATIC_PERMISSION_SETS::get);
        PermissionSet output = rolecall.getPermissions(role1, role2);
        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedEdit, output.edit()),
                () -> Assertions.assertEquals(expectedPublish, output.publish())
        );

    }

    @ParameterizedTest(name = "roles=[\"{0}\"]")
    @CsvSource({
            "Visitor, false, false",
            "User, true, true",
            "Admin, true, false",
            "Moderator, false, true"
    })
    @DisplayName("Only one to merge")
    void testMergeOnlyOne(String role, boolean expectedEdit, boolean expectedPublish) {
        Rolecall<PermissionSet> rolecall = new SimpleRolecall(STATIC_PERMISSION_SETS::get);
        PermissionSet output = rolecall.getPermissions(role);
        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedEdit, output.edit()),
                () -> Assertions.assertEquals(expectedPublish, output.publish())
        );

    }

    @Test
    @DisplayName("No roles given")
    void testNoRoles() {
        Rolecall<PermissionSet> rolecall = new SimpleRolecall(STATIC_PERMISSION_SETS::get);
        PermissionSet output = rolecall.getPermissions();
        Assertions.assertAll(
                () -> Assertions.assertFalse(output.edit()),
                () -> Assertions.assertFalse(output.publish())
        );
    }

    @Test
    @DisplayName("Null roles given")
    void testNullRoles() {
        Rolecall<PermissionSet> rolecall = new SimpleRolecall(STATIC_PERMISSION_SETS::get);
        PermissionSet output = rolecall.getPermissions(null);
        Assertions.assertAll(
                () -> Assertions.assertFalse(output.edit()),
                () -> Assertions.assertFalse(output.publish())
        );
    }

    @Test
    @DisplayName("Permissions not found")
    void testNoPermissions() {
        Rolecall<PermissionSet> rolecall = new SimpleRolecall(STATIC_PERMISSION_SETS::get);
        PermissionSet output = rolecall.getPermissions("Foobar");
        Assertions.assertAll(
                () -> Assertions.assertFalse(output.edit()),
                () -> Assertions.assertFalse(output.publish())
        );
    }

    @ParameterizedTest(name = "roles=[\"{0}\", \"{1}\", \"{2}\", \"{3}\"]")
    @CsvSource({
            "Admin, Visitor, Visitor, Visitor",
            "Visitor, Admin, Visitor, Visitor",
            "Visitor, Visitor, Admin, Visitor",
            "Visitor, Visitor, Visitor, Admin"
    })
    @DisplayName("Merging many roles in any order")
    void testMergeAllAnyOrder(String role1, String role2, String role3, String role4) {
        Rolecall<PermissionSet> rolecall = new SimpleRolecall(STATIC_PERMISSION_SETS::get);
        PermissionSet output = rolecall.getPermissions(role1, role2, role3, role4);
        Assertions.assertAll(
                () -> Assertions.assertTrue(output.edit()),
                () -> Assertions.assertFalse(output.publish())
        );

    }

    @ParameterizedTest(name = "roles=[\"{0}\", \"{1}\"]")
    @CsvSource({
            "Visitor, Foobar, false, false",
            "User, Foobar, true, true",
            "Admin, Foobar, true, false",
            "Moderator, Foobar, false, true"
    })
    @DisplayName("Merge valid role with an invalid one")
    void testMergeWithInvalidRole(String role1, String role2, boolean expectedEdit, boolean expectedPublish) {
        Rolecall<PermissionSet> rolecall = new SimpleRolecall(STATIC_PERMISSION_SETS::get);
        PermissionSet output = rolecall.getPermissions(role1, role2);
        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedEdit, output.edit()),
                () -> Assertions.assertEquals(expectedPublish, output.publish())
        );

    }

    @ParameterizedTest(name = "roles=[\"{0}\"]")
    @CsvSource({
            "Visitor",
            "User",
            "Admin",
            "Moderator"
    })
    @DisplayName("Permissions fetch returns null")
    void testFetchReturnsNull(String role) {
        Rolecall<PermissionSet> rolecall = new SimpleRolecall(STATIC_PERMISSION_SETS::get) {
            @Override
            protected PermissionSet[] fetchPermissionSets(String[] roles) {
                return null;
            }
        };
        PermissionSet output = rolecall.getPermissions(role);
        Assertions.assertAll(
                () -> Assertions.assertFalse(output.edit()),
                () -> Assertions.assertFalse(output.publish())
        );

    }

    @ParameterizedTest(name = "roles=[\"{0}\"]")
    @CsvSource({
            "Visitor, false, false",
            "User, true, true",
            "Admin, true, false",
            "Moderator, false, true"
    })
    @DisplayName("Permissions fetch return a null in array")
    void testFetchReturnANullInList(String role, boolean expectedEdit, boolean expectedPublish) {
        Rolecall<PermissionSet> rolecall = new SimpleRolecall(STATIC_PERMISSION_SETS::get) {
            @Override
            protected PermissionSet[] fetchPermissionSets(String[] roles) {
                PermissionSet[] sets = super.fetchPermissionSets(roles);
                PermissionSet[] result = new PermissionSet[sets.length + 1];
                for (int i = 0; i < sets.length; i++) {
                    result[i] = sets[i];
                }
                result[result.length-1] = null;
                return result;
            }
        };
        PermissionSet output = rolecall.getPermissions(role);
        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedEdit, output.edit()),
                () -> Assertions.assertEquals(expectedPublish, output.publish())
        );

    }

    @ParameterizedTest(name = "roles=[\"{0}\"]")
    @CsvSource({
            "Visitor",
            "User",
            "Admin",
            "Moderator"
    })
    @DisplayName("Merge returns null")
    void testMergeReturnsNull(String role) {
        Rolecall<PermissionSet> rolecall = new SimpleRolecall(STATIC_PERMISSION_SETS::get) {
            @Override
            protected PermissionSet[] fetchPermissionSets(String[] roles) {
                return new PermissionSet[]{null, null};
            }
        };
        PermissionSet output = rolecall.getPermissions(role);
        Assertions.assertAll(
                () -> Assertions.assertFalse(output.edit()),
                () -> Assertions.assertFalse(output.publish())
        );

    }
}
