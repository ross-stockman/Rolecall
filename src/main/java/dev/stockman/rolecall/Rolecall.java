package dev.stockman.rolecall;

public abstract class Rolecall<T> {
    /**
     * Resolves a set of roles to a single merged permission set.
     * <p>
     * Behavior:
     * - If {@code roles} is {@code null} or empty, returns {@link #defaultPermissionSet()}.
     * - Calls {@link #fetchPermissionSets(String...)} to obtain individual permission sets for the roles.
     * - If the fetched array is {@code null} or empty, returns {@link #defaultPermissionSet()}.
     * - Iterates the fetched array, skipping {@code null} elements, and merges non-null elements using {@link #merge(Object, Object)}.
     * - If all fetched elements are {@code null}, returns {@link #defaultPermissionSet()}.
     *
     * @param roles one or more role identifiers; may be {@code null} or empty
     * @return a single merged permission set representing the effective permissions for the provided roles;
     * returns the default permission set when inputs or fetched data are missing or unusable
     */
    public T getPermissions(String... roles) {
        if (roles == null || roles.length == 0) {
            return defaultPermissionSet();
        }
        T[] sets = fetchPermissionSets(roles);
        if (sets == null || sets.length == 0) {
            return defaultPermissionSet();
        }
        T merged = null;
        for (T set : sets) {
            if (set == null) continue;
            merged = (merged == null) ? set : merge(merged, set);
        }
        return (merged == null) ? defaultPermissionSet() : merged;
    }

    /**
     * Fetches zero or more permission sets corresponding to the provided roles.
     * <p>
     * Implementation contract:
     * - Should return an array (may be empty) and may return {@code null} to indicate no data available.
     * - Array elements may be {@code null}; they will be skipped by {@link #getPermissions(String...)}.
     * - Implementations are encouraged (but not required) to avoid returning {@code null} elements to reduce work upstream.
     *
     * @param roles role identifiers; never {@code null} here (the caller guards it), but may be empty
     * @return an array of permission sets (may be empty or {@code null}); elements may be {@code null}
     */
    protected abstract T[] fetchPermissionSets(String[] roles);

    /**
     * Merges two non-null permission sets into a single permission set.
     * <p>
     * Implementation contract:
     * - Both inputs will be non-null when called.
     * - Must return a non-null result.
     * - Merge semantics are domain-specific (e.g., boolean OR of permissions, union, most-permissive, etc.).
     *
     * @param first  the accumulated permission set
     * @param second the next permission set to merge
     * @return the merged permission set (must be non-null)
     */
    protected abstract T merge(T first, T second);

    /**
     * Returns the default permission set used as a fallback.
     * <p>
     * This value is returned when:
     * - Input roles are {@code null} or empty.
     * - {@link #fetchPermissionSets(String...)} returns {@code null} or an empty array.
     * - All fetched permission set elements are {@code null}.
     *
     * Implementation contract:
     * - Must return a non-null instance representing the baseline/no-permission state for the domain.
     *
     * @return the default permission set (must be non-null)
     */
    protected abstract T defaultPermissionSet();
}
