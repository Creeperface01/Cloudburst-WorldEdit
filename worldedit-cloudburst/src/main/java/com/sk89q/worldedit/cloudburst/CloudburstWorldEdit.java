/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.cloudburst;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.platform.PlatformReadyEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.anvil.ChunkDeleter;
import org.cloudburstmc.server.command.CommandSender;
import org.cloudburstmc.server.player.Player;
import org.cloudburstmc.server.plugin.PluginBase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.sk89q.worldedit.internal.anvil.ChunkDeleter.DELCHUNKS_FILE_NAME;

public class CloudburstWorldEdit extends PluginBase {

    private CloudburstPlatform platform;
    private CloudburstConfiguration config;

    @Override
    public void onEnable() {
        if (this.platform != null) {
            getLogger().warn("onEnable occurred for a second time, maybe it's reloaded? Disabling the plugin anyways");
            WorldEdit.getInstance().getPlatformManager().unregister(platform);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        final Path delChunks = getDataFolder().toPath().resolve(DELCHUNKS_FILE_NAME);
        if (Files.exists(delChunks)) {
            ChunkDeleter.runFromFile(delChunks, true);
        }

        loadConfig();
        WorldEdit.getInstance().getPlatformManager().register(platform);
        WorldEdit.getInstance().getEventBus().post(new PlatformReadyEvent());

        getLogger().info("WorldEdit for Sponge (version " + getDescription().getVersion() + ") is loaded");
    }

    public CloudburstPlatform getPlatform() {
        return platform;
    }

    public CloudburstPlayer wrapPlayer(Player player) {
        return new CloudburstPlayer(player);
    }

    public Actor wrapCommandSource(CommandSender sender) {
        if (sender instanceof Player) {
            return wrapPlayer((Player) sender);
        }

        return new CloudburstCommandSender(this, sender);
    }

    private void loadConfig() {
        createDefaultConfiguration("config.yml"); // Create the default configuration file

        config = new CloudburstConfiguration(new YAMLProcessor(new File(getDataFolder(), "config.yml"), true), this);
        config.load();
    }

    protected void createDefaultConfiguration(String name) {
        File actual = new File(getDataFolder(), name);
        if (!actual.exists()) {
            try (InputStream stream = getResource("defaults/" + name)) {
                if (stream == null) {
                    throw new FileNotFoundException();
                }
                copyDefaultConfig(stream, actual, name);
            } catch (IOException e) {
                getLogger().warn("Unable to read default configuration: " + name);
            }
        }
    }

    private void copyDefaultConfig(InputStream input, File actual, String name) {
        try (FileOutputStream output = new FileOutputStream(actual)) {
            byte[] buf = new byte[8192];
            int length;
            while ((length = input.read(buf)) > 0) {
                output.write(buf, 0, length);
            }

            getLogger().info("Default configuration file written: " + name);
        } catch (IOException e) {
            getLogger().warn("Failed to write default config file", e);
        }
    }

    protected void loadConfiguration() {
        config.unload();
        config.load();
    }

    public CloudburstConfiguration getConfiguration() {
        return config;
    }
}
