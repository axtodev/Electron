package lol.vifez.electron.util;

import java.util.regex.Pattern;

/*
 * Electron © Vifez
 * Developed by Vifez
 * Copyright (c) 2025 Vifez. All rights reserved.
 */

public class UUIDUtils {

    private static Pattern UUID_PATTERN = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0" +
            "-9a-fA-F]{3}-[0-9a-fA-F]{12}");

    public static boolean isUUID(String string) {
        return UUID_PATTERN.matcher(string).find();
    }
}
