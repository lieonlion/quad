package dev.lieonlion.quad.tags;

import dev.lieonlion.quad.Quad;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class QuadBlockTags {
    public static final TagKey<Block> COPPER_GOLEM_ITEM_DESTINATION = create("copper_golem_item_destination");

    private static TagKey<Block> create(String name) {
        return TagKey.create(Registries.BLOCK, Quad.location(name));
    }
}
