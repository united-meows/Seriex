/*
 * Copyright (C) 2017 TheE, Ronald Jack Jenkins Jr, SLF4Bukkit contributors.
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

import org.bukkit.ChatColor;

/**
 * Strips all {@link ChatColor}s from the input string.
 *
 * @author TheE
 */
final class NotSupportedColorMapper implements ColorMapper {

  @Override
  public String map(final String input) {
    if (input == null) {
      return "";
    }
    String output = input;
    for (final ChatColor chatColor : ChatColor.values()) {
      output = output.replace(chatColor.toString(), "");
    }
    return output;
  }

}
