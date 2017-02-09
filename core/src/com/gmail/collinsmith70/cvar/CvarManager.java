package com.gmail.collinsmith70.cvar;

import com.google.common.base.Objects;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.validator.Validator;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.util.Collection;
import java.util.Iterator;

/**
 * A <a href="https://en.wikipedia.org/wiki/Factory_method_pattern">factory</a> class for creating
 * and managing {@link Cvar} instances.
 * <p>
 * Invariant: If a {@code Cvar} is managed by a {@code CvarManager}, then
 * <ul>
 *   <li>{@link #get} with the {@link Cvar#getAlias()} must return the {@code Cvar}</li>
 *   <li>{@link Cvar#containsStateListener} with the {@code CvarManager} must evaluate {@code true}
 *   </li>
 * </ul>
 */
@SuppressWarnings({ "unused", "SimplifiableIfStatement" })
public class CvarManager implements Cvar.StateListener, Iterable<Cvar> {

  /**
   * A {@link Trie} mapping {@link Cvar} {@linkplain Cvar#getAlias() aliases} to themselves. This
   * mapping is necessary to keep track of which {@code Cvar} instances are managed by this
   * {@code CvarManager}, and implies that no two managed {@code Cvar} instances can have the same
   * alias.
   * <p>
   * In this implementation, {@code Cvar} aliases are to be stored in lower-case.
   */
  @NonNull
  private final Trie<String, Cvar> CVARS;

  /**
   * Constructs a new {@code CvarManager} instance.
   */
  public CvarManager() {
    this.CVARS = new PatriciaTrie<>();
  }

  /**
   * Returns a {@code Collection} of all {@code Cvar} instances {@linkplain #isManaging managed} by
   * this {@code CvarManager}.
   *
   * @return {@code Collection} of all {@code Cvar} instances managed by this {@code CvarManager}
   */
  @NonNull
  public Collection<Cvar> getCvars() {
    return CVARS.values();
  }

  /**
   * Returns an iterator over the {@link Cvar} instances managed by this {@code CvarManager}.
   *
   * @return An iterator over the {@link Cvar} instances managed by this {@code CvarManager}
   */
  @Override
  @NonNull
  public Iterator<Cvar> iterator() {
    return getCvars().iterator();
  }

  /**
   * Constructs and {@linkplain #add adds} a {@link Cvar} with the specified arguments to this
   * {@code CvarManager}.
   *
   * @param <T>          The {@linkplain Class type} of the {@linkplain Cvar#getValue variable}
   *                     which the {@code Cvar} represents
   * @param alias        The {@linkplain Cvar#getAlias name} of the {@code Cvar}
   * @param description  A brief {@linkplain Cvar#getDescription description} explaining the purpose
   *                     and function of the {@code Cvar} and values it permits
   * @param type         The {@linkplain T type} of the {@linkplain Cvar#getValue variable} that the
   *                     {@code Cvar} represents
   * @param defaultValue The {@linkplain Cvar#getDefaultValue default value} which will be assigned
   *                     to the {@code Cvar} now and whenever it is {@linkplain Cvar#reset}
   *
   * @return A reference to the created {@code Cvar}
   *
   * @see SimpleCvar#SimpleCvar
   * @see #add
   * @see #remove
   */
  @NonNull
  public <T> Cvar<T> create(@NonNull String alias, @NonNull String description,
                            @NonNull Class<T> type, @Nullable T defaultValue) {
    Cvar<T> cvar = new SimpleCvar<>(alias, description, type, defaultValue);
    add(cvar);
    return cvar;
  }

  /**
   * Constructs and {@linkplain #add adds} a {@link Cvar} with the specified arguments to this
   * {@code CvarManager}.
   *
   * @param <T>          The {@linkplain Class type} of the {@linkplain ValidatableCvar#getValue
   *                     variable} which the {@code Cvar} represents
   * @param alias        The {@linkplain ValidatableCvar#getAlias name} of the {@code Cvar}
   * @param description  A brief {@linkplain ValidatableCvar#getDescription description} explaining
   *                     the purpose and function of the {@code Cvar} and values it permits
   * @param type         The {@linkplain T type} of the {@linkplain ValidatableCvar#getValue
   *                     variable} that the {@code Cvar} represents
   * @param defaultValue The {@linkplain ValidatableCvar#getDefaultValue default value} which will
   *                     be assigned to the {@code Cvar} now and whenever it is
   *                     {@linkplain ValidatableCvar#reset}
   * @param validator    The {@code Validator} to use when {@linkplain ValidatableCvar#isValid
   *                     validating} {@linkplain ValidatableCvar#setValue assignments}
   *
   * @return A reference to the created {@code ValidatableCvar}
   *
   * @see ValidatableCvar#ValidatableCvar(String, String, Class, Object, Validator)
   * @see #add
   * @see #remove
   */
  @NonNull
  public <T> ValidatableCvar<T> create(@NonNull String alias, @NonNull String description,
                                       @NonNull Class<T> type, @Nullable T defaultValue,
                                       @NonNull Validator validator) {
    ValidatableCvar<T> cvar
            = new ValidatableCvar<>(alias, description, type, defaultValue, validator);
    add(cvar);
    return cvar;
  }

  /**
   * Adds the specified {@link Cvar} {@code cvar} to this {@code CvarManager}.
   * <p>
   * Note: Duplicate {@linkplain Cvar#getAlias aliases} are not allowed.
   * <p>
   * Note: If the specified {@code Cvar} is already being managed by this {@code CvarManager}, then
   *       this call does nothing and returns {@code false}.
   *
   * @param <T>  The {@linkplain Class type} of the {@linkplain Cvar#getValue variable} which the
   *             {@code Cvar} represents
   * @param cvar The {@code Cvar} instance to add for managing
   *
   * @return {@code true} if {@code cvar} was added and is now being managed by this
   *         {@code CvarManager}, otherwise {@code false}
   *
   * @throws IllegalArgumentException if there is already a {@code Cvar} instance being
   *     {@linkplain #isManaging managed} by this {@link CvarManager} with the same alias as
   *     {@code cvar}
   *
   * @see #create(String, String, Class, Object)
   * @see #create(String, String, Class, Object, Validator)
   * @see #remove(Cvar)
   */
  @SuppressWarnings("unchecked")
  public <T> boolean add(@NonNull Cvar<T> cvar) {
    final String alias = cvar.getAlias();
    final Cvar<T> queriedCvar = (Cvar<T>) CVARS.get(alias);
    if (Objects.equal(cvar, queriedCvar)) {
      return false;
    } else if (queriedCvar != null) {
      throw new IllegalArgumentException(String.format(
          "a cvar with the alias %s is already being managed by this CvarManager", alias));
    }

    CVARS.put(alias, cvar);
    return cvar.addStateListener(this);
  }

  /**
   * Removes the specified {@link Cvar} {@code cvar} from this {@code CvarManager}.
   * <p>
   * Note: If {@code cvar} is null, then {@code false} is returned.
   *
   * @param cvar The {@code Cvar} instance to remove (i.e., no longer manage)
   *
   * @return {@code true} if {@code cvar} was removed by this operation, otherwise {@code false}
   *
   * @see #create(String, String, Class, Object)
   * @see #create(String, String, Class, Object, Validator)
   * @see #add
   */
  public boolean remove(@Nullable Cvar cvar) {
    if (cvar == null) {
      return false;
    }

    String alias = cvar.getAlias();
    Cvar queriedCvar = CVARS.get(alias);
    if (cvar.equals(queriedCvar)) {
      return false;
    }

    return CVARS.remove(alias) != null;
  }

  /**
   * Searches for all {@link Cvar} the instances {@linkplain #isManaging managed} by this
   * {@code CvarManager} with {@linkplain Cvar#getAlias() aliases} containing the specified string
   * (i.e., partial matches are valid and expected) in lexicographical order.
   * <p>
   * Note: This operation is performed case-insensitively.
   * <p>
   * Note: If {@code alias} is {@code null}, then all aliases are returned.
   * <p>
   * Note: If the exact alias is known, and only one result is expected, use {@link #get} instead.
   *
   * @param alias The partial or exact {@code Cvar} {@linkplain Cvar#getAlias() alias} to query
   *
   * @return A {@link Collection} of all {@code Cvar} instances with aliases containing the
   *         specified string
   *
   * @see #get
   */
  @NonNull
  public Collection<Cvar> search(@Nullable String alias) {
    return CVARS.prefixMap(alias).values();
  }

  /**
   * Searches for a {@link Cvar} instance {@linkplain #isManaging managed} by this
   * {@code CvarManager} with the specified {@linkplain Cvar#getAlias() alias}.
   * <p>
   * Note: This operation is performed case-insensitively, however there must be an exact character
   *       match with some {@code Cvar} for it to be returned by this operation. If only a partial
   *       {@linkplain Cvar#getAlias() alias} is known, then {@link #search} may be a better option.
   * <p>
   * Note: If {@code alias} is {@code null}, then {@code null} is returned.
   *
   * @param <T>   The {@linkplain Class type} of the {@linkplain Cvar#getValue variable} which the
   *              {@code Cvar} represents
   * @param alias The {@code Cvar} alias to search for
   *
   * @return A {@code Cvar} with the specified alias, otherwise {@code null} if no {@code Cvar} with
   *         that alias is being managed by this {@link CvarManager}
   *
   * @throws ClassCastException if the {@code Cvar} with the specified {@code alias} cannot be cast
   *     to the given return type.
   *
   * @see #search
   */
  @Nullable
  @SuppressWarnings("unchecked")
  public <T> Cvar<T> get(@Nullable String alias) {
    if (alias == null) {
      return null;
    }

    return (Cvar<T>) CVARS.get(alias);
  }

  /**
   * Returns whether or not the specified {@link Cvar} is being managed by this {@code CvarManager}.
   * If a {@code Cvar} is being managed by a {@code CvarManager}, then the {@code CvarManager} can
   * be used to perform lookups for that {@code Cvar}.
   * <p>
   * Note: If {@code cvar} is {@code null}, then {@code false} is returned.
   *
   * @param cvar The {@code Cvar} to check
   *
   * @return {@code true} if the specified {@code Cvar} is being managed by this
   *         {@code CvarManager}, otherwise {@code false}
   */
  public boolean isManaging(@Nullable Cvar cvar) {
    if (cvar == null) {
      return false;
    }

    Cvar queriedCvar = CVARS.get(cvar.getAlias());
    return cvar.equals(queriedCvar);
  }

  /**
   * Returns whether or not a {@link Cvar} is being {@linkplain #isManaging managed} by this
   * {@code CvarManager} with the specified {@linkplain Cvar#getAlias() alias}.
   * <p>
   * Note: This operation is performed case-insensitively, however there must be an exact character
   *       match with some {@code Cvar} for {@code true} to be returned by this operation.
   * <p>
   * Note: If {@code cvar} is {@code null}, then {@code false} is returned.
   *
   * @param alias The {@linkplain Cvar#getAlias name} of the {@code Cvar} to check
   *
   * @return {@code true} if this {@code CvarManager} is managing a {@code Cvar} with the specified
   *         {@code alias}, otherwise {@code false}
   *
   * @see #get
   */
  public boolean containsAlias(@Nullable String alias) {
    return alias != null && CVARS.containsKey(alias);
  }

  @Override
  public void onChanged(@NonNull Cvar cvar, @Nullable Object from, @Nullable Object to) {}

  @Override
  public void onLoaded(@NonNull Cvar cvar, @Nullable Object to) {}

}
