/*
 * Copyright (C) 2016 Ronald Jack Jenkins Jr.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package info.ronjenkins.slf4bukkit;

import java.util.Collections;
import java.util.Iterator;

import org.bukkit.ChatColor;
import org.slf4j.Marker;

/**
 * SLF4J markers that map to a subset of {@link ChatColor}s. These markers never
 * contain any references (other markers).
 *
 * <p>
 * This class does not depend on JAnsi, so it is safe to use even in
 * environments where JAnsi is not available (e.g. PaperSpigot).
 * </p>
 *
 * @author Ronald Jack Jenkins Jr.
 */
public enum ColorMarker implements Marker {

  AQUA(ChatColor.AQUA), BLACK(ChatColor.BLACK), BLUE(ChatColor.BLUE),
  DARK_AQUA(ChatColor.DARK_AQUA), DARK_BLUE(ChatColor.DARK_BLUE),
  DARK_GRAY(ChatColor.DARK_GRAY), DARK_GREEN(ChatColor.DARK_GREEN),
  DARK_PURPLE(ChatColor.DARK_PURPLE), DARK_RED(ChatColor.DARK_RED),
  GOLD(ChatColor.GOLD), GRAY(ChatColor.GRAY), GREEN(ChatColor.GREEN),
  LIGHT_PURPLE(ChatColor.LIGHT_PURPLE), NONE(ChatColor.RESET),
  RED(ChatColor.RED), WHITE(ChatColor.WHITE), YELLOW(ChatColor.YELLOW);

  private final ChatColor value;

  private ColorMarker(final ChatColor value) {
    this.value = value;
  }

  /**
   * Not supported.
   *
   * @param reference
   *          unused.
   * @throws UnsupportedOperationException
   *           always.
   */
  @Override
  public void add(final Marker reference) {
    throw new UnsupportedOperationException();
  }

  /*
   * Marker API
   */

  /**
   * These markers never have references.
   *
   * @return false.
   */
  @Override
  public boolean contains(final Marker other) {
    return false;
  }

  /**
   * These markers never have references.
   *
   * @return false.
   */
  @Override
  public boolean contains(final String name) {
    return false;
  }

  /**
   * Returns the enum name of this marker.
   *
   * @return never null.
   */
  @Override
  public String getName() {
    return this.name();
  }

  /**
   * Returns the Bukkit color object associated with this marker.
   *
   * @return never null.
   */
  public ChatColor getValue() {
    return this.value;
  }

  /**
   * These markers never have references.
   *
   * @return false.
   */
  @Override
  @SuppressWarnings({ "all", "deprecation" })
  public boolean hasChildren() {
    return false;
  }

  /**
   * These markers never have references.
   *
   * @return false.
   */
  @Override
  public boolean hasReferences() {
    return false;
  }

  /**
   * These markers never have references.
   *
   * @return false.
   */
  @Override
  public Iterator<Marker> iterator() {
    return Collections.emptyIterator();
  }

  /**
   * Not supported.
   *
   * @param reference
   *          unused.
   * @throws UnsupportedOperationException
   *           always.
   */
  @Override
  public boolean remove(final Marker reference) {
    throw new UnsupportedOperationException();
  }

}
