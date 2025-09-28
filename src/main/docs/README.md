# Rolecall

Rolecall is a tiny Java utility that resolves a collection of role identifiers into a single, effective permission set. It abstracts fetching role-based permission sets and merging them into one result, with sensible fallbacks when inputs or data are missing.

## Features

- Merge multiple role-derived permission sets into one
- Pluggable fetch strategy for retrieving permission sets by role
- Customizable merge semantics (e.g., permissive OR, intersection, union)
- Safe fallbacks:
    - Null/empty roles → default permission set
    - Null/empty fetched sets → default permission set
    - All-null fetched elements → default permission set

## Requirements

- Java ${java.version}
- Maven 3.x

## Dependency

Main dependencies include:
- JUnit Jupiter ${junit.version}

## How It Works

- getPermissions(String... roles)
    - Validates input roles
    - fetchPermissionSets(roles): returns an array of permission sets (nullable/empty allowed)
    - Merges non-null elements using merge(first, second)
    - Falls back to defaultPermissionSet() when nothing usable is available

- Abstract methods you implement:
    - fetchPermissionSets(String... roles)
        - May return null or an empty array
        - Elements may be null (they are skipped)
        - Prefer returning only non-null elements for efficiency
    - merge(T first, T second)
        - Inputs are non-null
        - Must return a non-null merged instance
    - defaultPermissionSet()
        - Must return a non-null baseline permission set

## Project Structure

- src/main/java/dev/stockman/rolecall/Rolecall
- src/test/java/dev/stockman/rolecall/SimpleRolecall (example)
- src/test/java/dev/stockman/rolecall/RolecallTest

## Building from Source

```bash
mvn clean install
```

## License

This project is licensed under the MIT License. See LICENSE for details.

## Contributing

Contributions are welcome!

- Fork the repository
- Create a feature branch
- Add tests for changes
- Keep coverage high
- Open a Pull Request with a clear description

## Example
Below is an example implementation of Rolecall.

```java

public record PermissionSet(boolean edit, boolean publish) {
}

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
```

Below is an example usage of the above implementation.

```java
    // we will use a static map for simplicity, but this could be a database or a remote API
    private static final Map<String, PermissionSet> STATIC_PERMISSION_SETS = Map.of(
            "Visitor", new PermissionSet(false, false),
            "User", new PermissionSet(true, true),
            "Admin", new PermissionSet(true, false),
            "Moderator", new PermissionSet(false, true)
    );

    // Initialize the rolecall with a function that fetches permission sets by role
    Rolecall<PermissionSet> rolecall = new SimpleRolecall(STATIC_PERMISSION_SETS::get);
    
    // Finally, get the effective permissions for the given list of roles
    PermissionSet permissions = rolecall.getPermissions("Visitor", "Admin");

```