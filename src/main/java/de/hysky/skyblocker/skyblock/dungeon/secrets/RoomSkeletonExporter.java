package de.hysky.skyblocker.skyblock.dungeon.secrets;

import de.hysky.skyblocker.skyblock.dungeon.preview.SkeletonBlock;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.DeflaterOutputStream;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public class RoomSkeletonExporter {
	private RoomSkeletonExporter() {}

	public static int[] collectBlocks(ClientLevel world, Room room) {
		Vector2ic physicalCorner = getNorthwestCorner(room.getSegments());
		List<SkeletonBlock> blocks = new ArrayList<>();

		for (Vector2ic segment : room.getSegments()) {
			for (int x = segment.x(); x <= segment.x() + 30; x++) {
				for (int z = segment.y(); z <= segment.y() + 30; z++) {
					BlockPos column = new BlockPos(x, 0, z);
					if (!world.hasChunkAt(column)) {
						throw new IllegalStateException("The entire room must be loaded before exporting it.");
					}

					for (int y = 0; y <= 255; y++) {
						BlockPos actual = new BlockPos(x, y, z);
						byte blockType = DungeonManager.NUMERIC_ID.getByte(BuiltInRegistries.BLOCK.getKey(world.getBlockState(actual).getBlock()).toString());
						if (blockType == 0) continue;

						BlockPos relative = DungeonMapUtils.actualToRelative(Room.Direction.NW, physicalCorner, actual);
						blocks.add(new SkeletonBlock(relative.getX(), relative.getY(), relative.getZ(), blockType));
					}
				}
			}
		}

		return createBlockArray(blocks);
	}

	public static Path createOutputPath(Path outputDirectory) throws IOException {
		Files.createDirectories(outputDirectory);
		return outputDirectory.resolve("room-" + UUID.randomUUID() + ".skeleton");
	}

	public static void write(Path outputPath, int[] blocks) throws IOException {
		try (ObjectOutputStream outputStream = new ObjectOutputStream(new DeflaterOutputStream(Files.newOutputStream(outputPath)))) {
			outputStream.writeObject(blocks);
		}
	}

	static int[] createBlockArray(List<SkeletonBlock> blocks) {
		return blocks.stream().mapToInt(SkeletonBlock::compress).sorted().toArray();
	}

	private static Vector2ic getNorthwestCorner(Iterable<Vector2ic> segments) {
		int x = Integer.MAX_VALUE;
		int z = Integer.MAX_VALUE;
		for (Vector2ic segment : segments) {
			x = Math.min(x, segment.x());
			z = Math.min(z, segment.y());
		}
		return new Vector2i(x, z);
	}
}
