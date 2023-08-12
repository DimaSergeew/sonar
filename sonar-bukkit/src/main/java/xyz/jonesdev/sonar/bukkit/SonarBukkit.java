/*
 * Copyright (C) 2023 Sonar Contributors
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.jonesdev.sonar.bukkit;

import lombok.Getter;
import org.bstats.bukkit.Metrics;
import xyz.jonesdev.sonar.api.Sonar;
import xyz.jonesdev.sonar.api.SonarPlatform;
import xyz.jonesdev.sonar.api.command.InvocationSender;
import xyz.jonesdev.sonar.api.logger.Logger;
import xyz.jonesdev.sonar.api.server.ServerWrapper;
import xyz.jonesdev.sonar.bukkit.command.SonarCommand;
import xyz.jonesdev.sonar.bukkit.verbose.ActionBarVerbose;
import xyz.jonesdev.sonar.common.SonarBootstrap;
import xyz.jonesdev.sonar.common.fallback.traffic.TrafficCounter;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

@Getter
public final class SonarBukkit extends SonarBootstrap<SonarBukkitPlugin> implements Sonar {
  public static SonarBukkit INSTANCE;

  public SonarBukkit(final SonarBukkitPlugin plugin) {
    super(plugin, plugin.getDataFolder(), new ActionBarVerbose(plugin.getServer()));
    INSTANCE = this;
  }

  private final Logger logger = new Logger() {

    @Override
    public void info(final String message, final Object... args) {
      getPlugin().getLogger().log(Level.INFO, message, args);
    }

    @Override
    public void warn(final String message, final Object... args) {
      getPlugin().getLogger().log(Level.WARNING, message, args);
    }

    @Override
    public void error(final String message, final Object... args) {
      getPlugin().getLogger().log(Level.SEVERE, message, args);
    }
  };

  /**
   * Create a wrapper object for our server, so we can use it outside
   * the velocity module.
   * We have to do this, so we can access all necessary API functions.
   *
   * @since 2.0.0 (7faa4b6)
   */
  public final ServerWrapper server = new ServerWrapper() {

    @Override
    public SonarPlatform getPlatform() {
      return SonarPlatform.VELOCITY;
    }

    @Override
    public Optional<InvocationSender> getOnlinePlayer(final String username) {
      return getPlugin().getServer().getOnlinePlayers().stream()
        .filter(player -> player.getName().equalsIgnoreCase(username))
        .findFirst()
        .map(player -> new InvocationSender() {

          @Override
          public String getName() {
            return player.getName();
          }

          @Override
          public void sendMessage(final String message) {
            player.sendMessage(message);
          }
        });
    }
  };

  @Override
  public void enable() {

    // Reload configuration
    reload();

    // Initialize bStats.org metrics
    new Metrics(getPlugin(), getServiceId());

    // Register Sonar command
    Objects.requireNonNull(getPlugin().getCommand("sonar")).setExecutor(new SonarCommand());

    // Register Fallback queue task
    getPlugin().getServer().getScheduler().runTaskTimerAsynchronously(getPlugin(), getFallback().getQueue()::poll,
      10L, 10L);

    // Register traffic counter reset task
    getPlugin().getServer().getScheduler().runTaskTimerAsynchronously(getPlugin(), TrafficCounter::reset,
      20L, 20L);

    // Register action bar verbose task
    getPlugin().getServer().getScheduler().runTaskTimerAsynchronously(getPlugin(), getActionBarVerbose()::update,
      2L, 2L);
  }
}
