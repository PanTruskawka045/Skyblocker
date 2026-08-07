package de.hysky.skyblocker.skyblock.dungeon.secrets;

import de.hysky.skyblocker.skyblock.dungeon.preview.SkeletonBlock;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.InflaterInputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RoomSkeletonExporterTest {
	@Test
	void createsSortedSkeletonBlockArray() {
		int[] blocks = RoomSkeletonExporter.createBlockArray(List.of(
				new SkeletonBlock(2, 70, 2, (byte) 1),
				new SkeletonBlock(1, 70, 2, (byte) 1)
		));

		Assertions.assertArrayEquals(new int[]{0x01460201, 0x02460201}, blocks);
	}

	@Test
	void writesSkeletonInTheFormatUsedByRoomMatching() throws Exception {
		Path output = Files.createTempFile("room", ".skeleton");
		int[] blocks = {0x01460201, 0x02460201};

		RoomSkeletonExporter.write(output, blocks);

		try (ObjectInputStream inputStream = new ObjectInputStream(new InflaterInputStream(Files.newInputStream(output)))) {
			Assertions.assertArrayEquals(blocks, (int[]) inputStream.readObject());
		}
	}
}
