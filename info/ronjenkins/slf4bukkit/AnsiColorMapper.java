/*
 * Copyright (C) 2016-2017 Ronald Jack Jenkins Jr., SLF4Bukkit contributors.
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

import com.google.common.collect.ImmutableMap;

import org.bukkit.ChatColor;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Attribute;

import java.util.Map;

/**
 * Maps {@link ChatColor} values to their JAnsi equivalents.
 *
 * @author Ronald Jack Jenkins Jr.
 */
final class AnsiColorMapper implements ColorMapper {

  /**
   * No-op constructor. Callers must catch {@code Throwable} and handle the
   * scenario in which JAnsi is not available by substituting another
   * {@link ColorMapper} implementation.
   *
   * @throws Throwable if JAnsi is not present at runtime.
   */
  AnsiColorMapper() throws Throwable {}

  // @formatter:off
  private final Map<ChatColor, String> MAP = ImmutableMap.<ChatColor, String>builder()
    .put(ChatColor.BLACK, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.BLACK).boldOff().toString())
    .put(ChatColor.DARK_BLUE, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.BLUE).boldOff().toString())
    .put(ChatColor.DARK_GREEN, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.GREEN).boldOff().toString())
    .put(ChatColor.DARK_AQUA, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.CYAN).boldOff().toString())
    .put(ChatColor.DARK_RED, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.RED).boldOff().toString())
    .put(ChatColor.DARK_PURPLE, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.MAGENTA).boldOff().toString())
    .put(ChatColor.GOLD, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.YELLOW).boldOff().toString())
    .put(ChatColor.GRAY, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.WHITE).boldOff().toString())
    .put(ChatColor.DARK_GRAY, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.BLACK).bold().toString())
    .put(ChatColor.BLUE, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.BLUE).bold().toString())
    .put(ChatColor.GREEN, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.GREEN).bold().toString())
    .put(ChatColor.AQUA, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.CYAN).bold().toString())
    .put(ChatColor.RED, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.RED).bold().toString())
    .put(ChatColor.LIGHT_PURPLE, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.MAGENTA).bold().toString())
    .put(ChatColor.YELLOW, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.YELLOW).bold().toString())
    .put(ChatColor.WHITE, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.WHITE).bold().toString())
    .put(ChatColor.MAGIC, Ansi.ansi().a(Attribute.BLINK_SLOW).toString())
    .put(ChatColor.BOLD, Ansi.ansi().a(Attribute.UNDERLINE_DOUBLE).toString())
    .put(ChatColor.STRIKETHROUGH, Ansi.ansi().a(Attribute.STRIKETHROUGH_ON).toString())
    .put(ChatColor.UNDERLINE, Ansi.ansi().a(Attribute.UNDERLINE).toString())
    .put(ChatColor.ITALIC, Ansi.ansi().a(Attribute.ITALIC).toString())
    .put(ChatColor.RESET, Ansi.ansi().a(Attribute.RESET).toString())
  .build();
  // @formatter:on

  @Override
  public String map(final String input) {
    if (input == null) {
      return "";
    }
    String output = input;
    for (final Map.Entry<ChatColor, String> mapping : MAP.entrySet()) {
      output = output.replace(mapping.getKey().toString(), mapping.getValue());
    }
    return output;
  }

}
