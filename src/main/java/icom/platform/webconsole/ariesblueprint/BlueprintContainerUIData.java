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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Generated;

import org.apache.aries.blueprint.container.BlueprintContainerImpl;
import org.apache.aries.blueprint.di.Recipe;
import org.apache.aries.blueprint.di.Repository;
import org.osgi.framework.Bundle;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.blueprint.container.BlueprintEvent;

/**
 * The information that is shown about a blueprint container on the screen.
 */
public class BlueprintContainerUIData implements Comparable<BlueprintContainerUIData> {

  private Bundle bundle;

  private Throwable cause;

  private int eventType;

  private String[] missingDependencies;

  private Set<RecipeUIData> recipes;

  private long timestamp;

  private int unsatisfiedReferenceNum;

  /**
   * Constructor.
   *
   * @param blueprintEvent
   *          The Blueprint event that this ui data belongs to.
   * @param blueprintContainer
   *          The Blueprint container that this ui data belongs to.
   */
  public BlueprintContainerUIData(final BlueprintEvent blueprintEvent,
      final BlueprintContainer blueprintContainer) {
    if (!(blueprintContainer instanceof BlueprintContainerImpl)) {
      throw new IllegalArgumentException(
          "Blueprint container is not instance of repository: " + blueprintContainer);
    }

    BlueprintContainerImpl blueprintContainerImpl = (BlueprintContainerImpl) blueprintContainer;

    bundle = blueprintContainerImpl.getBundleContext().getBundle();

    Repository repository = blueprintContainerImpl.getRepository();
    Set<Recipe> recipes = repository.getAllRecipes();

    Set<RecipeUIData> recipeUIDataSet = new TreeSet<>();
    for (Recipe recipe : recipes) {
      recipeUIDataSet.add(new RecipeUIData(recipe));
    }
    this.recipes = recipeUIDataSet;

    eventType = blueprintEvent.getType();
    cause = blueprintEvent.getCause();
    timestamp = blueprintEvent.getTimestamp();
    missingDependencies = blueprintEvent.getDependencies();

    unsatisfiedReferenceNum = resolveUnsatisfiedReferenceNum();
  }

  @Override
  public int compareTo(final BlueprintContainerUIData o) {
    int result = Long.compare(timestamp, o.timestamp);
    if (result != 0) {
      return result;
    }

    result = bundle.getSymbolicName().compareTo(o.bundle.getSymbolicName());
    if (result != 0) {
      return result;
    }

    result = bundle.getVersion().compareTo(o.bundle.getVersion());
    return result;
  }

  public Bundle getBundle() {
    return bundle;
  }

  public Throwable getCause() {
    return cause;
  }

  @Override
  @Generated("eclipse")
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((bundle == null) ? 0 : bundle.hashCode());
    result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
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
    BlueprintContainerUIData other = (BlueprintContainerUIData) obj;
    if (bundle == null) {
      if (other.bundle != null)
        return false;
    } else if (!bundle.equals(other.bundle))
      return false;
    if (timestamp != other.timestamp)
      return false;
    return true;
  }

  /**
   * Get the stacktrace of an optional error cause that arrived with the blueprint event.
   *
   * @return The stacktrace of the error or <code>null</code> if not available.
   */
  public String getCauseStackTrace() {
    if (cause == null) {
      return "";
    }
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    cause.printStackTrace(pw);
    return sw.toString();
  }

  /**
   * Get the name of the type of the last {@link BlueprintEvent} of this container.
   *
   * @return The name of the blueprint event.
   */
  public String getEventTypeName() {
    switch (eventType) {
      case BlueprintEvent.CREATED:
        return "Created";
      case BlueprintEvent.CREATING:
        return "Creating";
      case BlueprintEvent.DESTROYED:
        return "Destroyed";
      case BlueprintEvent.DESTROYING:
        return "Destroying";
      case BlueprintEvent.FAILURE:
        return "Failure";
      case BlueprintEvent.GRACE_PERIOD:
        return "Grace period";
      case BlueprintEvent.WAITING:
        return "Waiting";
      default:
        throw new RuntimeException("Unknown event type: " + eventType);
    }
  }

  public String getMissingDependenciesString() {
    return (missingDependencies != null) ? Arrays.toString(missingDependencies) : "";
  }

  public Set<RecipeUIData> getRecipes() {
    return recipes;
  }

  public Date getTimestampDate() {
    return new Date(timestamp);
  }

  public int getUnsatisfiedReferenceNum() {
    return unsatisfiedReferenceNum;
  }

  private int resolveUnsatisfiedReferenceNum() {
    int result = 0;
    for (RecipeUIData recipe : recipes) {
      if ((recipe.getSatisfied() != null) && !recipe.getSatisfied().booleanValue()) {
        result++;
      }
    }
    return result;
  }
}
