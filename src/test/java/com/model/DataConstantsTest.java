package com.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link DataConstants#resolveDataPath(String)} for cwd vs nested project layout.
 */
public class DataConstantsTest {

    private static final String MARKER_REL = "target/data_constants_test_78/marker.json";

    private Path markerPath;

    @Before
    public void setUp() throws IOException {
        markerPath = Paths.get(MARKER_REL);
        Files.createDirectories(markerPath.getParent());
        Files.writeString(markerPath, "{}", StandardCharsets.UTF_8);
    }

    @After
    public void tearDown() throws IOException {
        if (markerPath != null) {
            Files.deleteIfExists(markerPath);
            Path parent = markerPath.getParent();
            if (parent != null) {
                Files.deleteIfExists(parent);
            }
        }
    }

    @Test
    public void resolveDataPath_returnsRelativeStringWhenFileExistsAtRelativePath() {
        String resolved = DataConstants.resolveDataPath(MARKER_REL);
        assertEquals(MARKER_REL, resolved);
        assertTrue(new File(resolved).exists());
    }

    @Test
    public void resolveDataPath_returnsLostCodingHelperPathWhenRunningFromParentFolder() {
        File direct = new File(DataConstants.USER_FILE_NAME);
        File nested = new File("lost_coding_helper/" + DataConstants.USER_FILE_NAME);
        assumeTrue("Skip when json already exists next to cwd (e.g. mvn from module root).",
                !direct.exists() && nested.exists());

        String resolved = DataConstants.resolveDataPath(DataConstants.USER_FILE_NAME);
        assertTrue(new File(resolved).exists());
        String normalized = resolved.replace('\\', '/');
        assertTrue(normalized.contains("lost_coding_helper"));
    }

    @Test
    public void resolveDataPath_returnsInputWhenNeitherLocationExists() {
        String missing = "no_such_folder_78/ghost.json";
        assertFalse(new File(missing).exists());
        assertFalse(new File("lost_coding_helper/" + missing).exists());

        String resolved = DataConstants.resolveDataPath(missing);
        assertEquals(missing, resolved);
    }

    @Test(expected = NullPointerException.class)
    public void resolveDataPath_throwsWhenRelativePathNull() {
        DataConstants.resolveDataPath(null);
    }

    /*
     * Simple summary for our group / teacher:
     * This class tests resolveDataPath which picks where the json files live depending on what
     * folder you run the program from. We check a fake file in target, a path that doesnt exist,
     * and null. One test only runs if you start from the parent folder next to lost_coding_helper
     * otherwise it gets skipped and thats ok.
     */
}
