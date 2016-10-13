/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package icom.platform.webconsole.ariesblueprint;

import javax.annotation.Generated;

import org.apache.aries.blueprint.container.SatisfiableRecipe;
import org.apache.aries.blueprint.di.Recipe;

/**
 * The information that is shown about a {@link Recipe} on the screen.
 */
public class RecipeUIData implements Comparable<RecipeUIData> {

  private static <T extends Comparable<T>> int compareNullable(final T obj1, final T obj2,
      final boolean nullLower) {
    if ((obj1 == null) && (obj2 != null)) {
      return (nullLower) ? -1 : 1;
    }

    if ((obj1 != null) && (obj2 == null)) {
      return (nullLower) ? 1 : -1;
    }

    if ((obj1 == null) || (obj1 == obj2)) {
      return 0;
    }

    return obj1.compareTo(obj2);
  }

  private final String name;

  private final String osgiFilter;

  private final Boolean satisfied;

  /**
   * Constructor.
   *
   * @param recipe
   *          The {@link Recipe} instance that this ui class contains information about.
   */
  public RecipeUIData(final Recipe recipe) {
    name = recipe.getName();

    if (recipe instanceof SatisfiableRecipe) {
      SatisfiableRecipe satisfiableRecipe = (SatisfiableRecipe) recipe;
      satisfied = satisfiableRecipe.isSatisfied();
      osgiFilter = satisfiableRecipe.getOsgiFilter();
    } else {
      satisfied = null;
      osgiFilter = null;
    }
  }



  @Override
  @Generated("eclipse")
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((osgiFilter == null) ? 0 : osgiFilter.hashCode());
    result = prime * result + ((satisfied == null) ? 0 : satisfied.hashCode());
    return result;
  }



  @Override
  @Generated("eclipse")
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RecipeUIData other = (RecipeUIData) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (osgiFilter == null) {
      if (other.osgiFilter != null)
        return false;
    } else if (!osgiFilter.equals(other.osgiFilter))
      return false;
    if (satisfied == null) {
      if (other.satisfied != null)
        return false;
    } else if (!satisfied.equals(other.satisfied))
      return false;
    return true;
  }



  @Override
  public int compareTo(final RecipeUIData o) {
    int result = RecipeUIData.compareNullable(satisfied, o.satisfied, false);
    if (result != 0) {
      return result;
    }

    return name.compareTo(o.name);
  }

  public String getName() {
    return name;
  }

  public String getOsgiFilter() {
    return osgiFilter;
  }

  public Boolean getSatisfied() {
    return satisfied;
  }
}
