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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.nukkitx.nbt.NbtMap;
import org.cloudburstmc.server.block.BlockPalette;
import org.cloudburstmc.server.block.BlockState;

import java.util.Locale;

public class CloudUtils {

    public static final BiMap<String, BlockState> typeMapping = HashBiMap.create();

    public static BlockState adapt(String type) {
        return typeMapping.get(type);
    }

    public static String adapt(BlockState state) {
        //return state.toString().toLowerCase(Locale.ROOT); TODO: check this
        String existing = typeMapping.inverse().get(state);
        if (existing != null) {
            return existing;
        }

        NbtMap serialized = BlockPalette.INSTANCE.getSerialized(state);

        StringBuilder builder = new StringBuilder();
        builder.append(serialized.getString("name"));
        NbtMap states = serialized.getCompound("states");
        if (states != null && !states.isEmpty() && state != state.defaultState()) {
            builder.append('{');
            states.forEach((trait, value) -> builder.append(trait)
                    .append('=')
                    .append(value.toString())
                    .append(',')
                    .append(' ')
            );
            builder.setLength(builder.length() - 1);
            builder.setCharAt(builder.length() - 1, '}');
        }

        return builder.toString().toLowerCase(Locale.ROOT);
    }
}
