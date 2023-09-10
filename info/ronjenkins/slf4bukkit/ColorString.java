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

/**
 * A {@link StringBuilder}-like class with a fluent API for adding content
 * colored via {@link ColorMarker}s. You may call {@link #toString()} to get the
 * current value and then continue adding content to this object. This class is
 * thread-safe.
 *
 * <p>
 * Plugins can use this class even if they will be executed in environments
 * where JAnsi is not available (e.g. PaperSpigot) because all colors are
 * stripped when the message is logged. This class does not depend on JAnsi,
 * so it is safe to use in such environments.
 * </p>
 *
 * @author Ronald Jack Jenkins Jr.
 */
public final class ColorString {

  private ColorMarker         currentColor = ColorMarker.NONE;
  private final StringBuilder value        = new StringBuilder();

  /** Constructor. */
  public ColorString() {
  }

  /**
   * Sets the color to {@link ColorMarker#AQUA}, then calls
   * {@link StringBuilder#append(String)}.
   *
   * @param append
   *          the string to append.
   * @return this.
   */
  public ColorString aqua(final String append) {
    synchronized (this.value) {
      this.setColor(ColorMarker.AQUA);
      this.value.append(append);
      return this;
    }
  }

  /**
   * Sets the color to {@link ColorMarker#BLACK}, then calls
   * {@link StringBuilder#append(String)}.
   *
   * @param append
   *          the string to append.
   * @return this.
   */
  public ColorString black(final String append) {
    synchronized (this.value) {
      this.setColor(ColorMarker.BLACK);
      this.value.append(append);
      return this;
    }
  }

  /**
   * Sets the color to {@link ColorMarker#BLUE}, then calls
   * {@link StringBuilder#append(String)}.
   *
   * @param append
   *          the string to append.
   * @return this.
   */
  public ColorString blue(final String append) {
    synchronized (this.value) {
      this.setColor(ColorMarker.BLUE);
      this.value.append(append);
      return this;
    }
  }

  /**
   * Sets the color to {@link ColorMarker#DARK_AQUA}, then calls
   * {@link StringBuilder#append(String)}.
   *
   * @param append
   *          the string to append.
   * @return this.
   */
  public ColorString darkAqua(final String append) {
    synchronized (this.value) {
      this.setColor(ColorMarker.DARK_AQUA);
      this.value.append(append);
      return this;
    }
  }

  /**
   * Sets the color to {@link ColorMarker#DARK_BLUE}, then calls
   * {@link StringBuilder#append(String)}.
   *
   * @param append
   *          the string to append.
   * @return this.
   */
  public ColorString darkBlue(final String append) {
    synchronized (this.value) {
      this.setColor(ColorMarker.DARK_BLUE);
      this.value.append(append);
      return this;
    }
  }

  /**
   * Sets the color to {@link ColorMarker#DARK_GRAY}, then calls
   * {@link StringBuilder#append(String)}.
   *
   * @param append
   *          the string to append.
   * @return this.
   */
  public ColorString darkGray(final String append) {
    synchronized (this.value) {
      this.setColor(ColorMarker.DARK_GRAY);
      this.value.append(append);
      return this;
    }
  }

  /**
   * Sets the color to {@link ColorMarker#DARK_GREEN}, then calls
   * {@link StringBuilder#append(String)}.
   *
   * @param append
   *          the string to append.
   * @return this.
   */
  public ColorString darkGreen(final String append) {
    synchronized (this.value) {
      this.setColor(ColorMarker.DARK_GREEN);
      this.value.append(append);
      return this;
    }
  }

  /**
   * Sets the color to {@link ColorMarker#DARK_RED}, then calls
   * {@link StringBuilder#append(String)}.
   *
   * @param append
   *          the string to append.
   * @return this.
   */
  public ColorString darkRed(final String append) {
    synchronized (this.value) {
      this.setColor(ColorMarker.DARK_RED);
      this.value.append(append);
      return this;
    }
  }

  /**
   * Sets the color to {@link ColorMarker#GOLD}, then calls
   * {@link StringBuilder#append(String)}.
   *
   * @param append
   *          the string to append.
   * @return this.
   */
  public ColorString gold(final String append) {
    synchronized (this.value) {
      this.setColor(ColorMarker.GOLD);
      this.value.append(append);
      return this;
    }
  }

  /**
   * Sets the color to {@link ColorMarker#GRAY}, then calls
   * {@link StringBuilder#append(String)}.
   *
   * @param append
   *          the string to append.
   * @return this.
   */
  public ColorString gray(final String append) {
    synchronized (this.value) {
      this.setColor(ColorMarker.GRAY);
      this.value.append(append);
      return this;
    }
  }

  /**
   * Sets the color to {@link ColorMarker#GREEN}, then calls
   * {@link StringBuilder#append(String)}.
   *
   * @param append
   *          the string to append.
   * @return this.
   */
  public ColorString green(final String append) {
    synchronized (this.value) {
      this.setColor(ColorMarker.GREEN);
      this.value.append(append);
      return this;
    }
  }

  /**
   * Resets all formatting at the current position, then calls
   * {@link StringBuilder#append(String)}.
   *
   * @param append
   *          the string to append.
   * @return this.
   */
  public ColorString none(final String append) {
    synchronized (this.value) {
      this.setColor(ColorMarker.NONE);
      this.value.append(append);
      return this;
    }
  }

  /**
   * Sets the color to {@link ColorMarker#LIGHT_PURPLE}, then calls
   * {@link StringBuilder#append(String)}.
   *
   * @param append
   *          the string to append.
   * @return this.
   */
  public ColorString pink(final String append) {
    synchronized (this.value) {
      this.setColor(ColorMarker.LIGHT_PURPLE);
      this.value.append(append);
      return this;
    }
  }

  /**
   * Sets the color to {@link ColorMarker#DARK_PURPLE}, then calls
   * {@link StringBuilder#append(String)}.
   *
   * @param append
   *          the string to append.
   * @return this.
   */
  public ColorString purple(final String append) {
    synchronized (this.value) {
      this.setColor(ColorMarker.DARK_PURPLE);
      this.value.append(append);
      return this;
    }
  }

  /**
   * Sets the color to {@link ColorMarker#RED}, then calls
   * {@link StringBuilder#append(String)}.
   *
   * @param append
   *          the string to append.
   * @return this.
   */
  public ColorString red(final String append) {
    synchronized (this.value) {
      this.setColor(ColorMarker.RED);
      this.value.append(append);
      return this;
    }
  }

  /**
   * Resets all formatting at the current position, then returns the string's
   * value.
   *
   * @return never null.
   */
  @Override
  public String toString() {
    synchronized (this.value) {
      this.setColor(ColorMarker.NONE);
      return this.value.toString();
    }
  }

  /**
   * Appends the given color to the string sequence, then returns the string's
   * value.
   *
   * @param color
   *          the desired color suffix. Null is coerced to
   *          {@link ColorMarker#NONE}, which resets all formatting at the
   *          current position.
   * @return never null. Returns {@link #toString()} if the color is null.
   */
  public String toString(final ColorMarker color) {
    synchronized (this.value) {
      if (color == null) { return this.toString(); }
      this.setColor(color);
      return this.value.toString();
    }
  }

  /**
   * Sets the color to {@link ColorMarker#WHITE}, then calls
   * {@link StringBuilder#append(String)}.
   *
   * @param append
   *          the string to append.
   * @return this.
   */
  public ColorString white(final String append) {
    synchronized (this.value) {
      this.setColor(ColorMarker.WHITE);
      this.value.append(append);
      return this;
    }
  }

  /**
   * Sets the color to {@link ColorMarker#YELLOW}, then calls
   * {@link StringBuilder#append(String)}.
   *
   * @param append
   *          the string to append.
   * @return this.
   */
  public ColorString yellow(final String append) {
    synchronized (this.value) {
      this.setColor(ColorMarker.YELLOW);
      this.value.append(append);
      return this;
    }
  }

  private void setColor(final ColorMarker color) {
    synchronized (this.value) {
      if (color != this.currentColor) {
        this.value.append(color.getValue());
        this.currentColor = color;
      }
    }
  }

}
