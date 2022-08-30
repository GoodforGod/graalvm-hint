package io.goodforgod.graalvm.hint.processor;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 30.08.2022
 */
final class HintFile {

    private final String name;
    private final String path;

    HintFile(String fileName, String filePath) {
        this.name = fileName;
        this.path = filePath;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path + "/" + name;
    }

    @Override
    public String toString() {
        return "[file=" + getPath() + ']';
    }
}
