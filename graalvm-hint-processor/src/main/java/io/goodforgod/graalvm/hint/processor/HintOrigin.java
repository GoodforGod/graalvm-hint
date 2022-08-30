package io.goodforgod.graalvm.hint.processor;

/**
 * Hint origin package and artifact where all configs will be generated
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.09.2021
 */
final class HintOrigin {

    public static final String HINT_PROCESSING_GROUP = "graalvm.hint.group";
    public static final String HINT_PROCESSING_ARTIFACT = "graalvm.hint.artifact";

    static final String DEFAULT_PACKAGE = "io.graalvm.hint";
    static final String DEFAULT_ARTIFACT = "hint";

    /**
     * Artifact group of project
     */
    private final String group;

    /**
     * Artifact name of project
     */
    private final String artifact;

    HintOrigin(String group, String artifact) {
        this.group = group;
        this.artifact = artifact;
    }

    public HintFile getFileWithRelativePath(String fileName) {
        return new HintFile(fileName, "META-INF/native-image/" + group + "/" + artifact + "/");
    }

    @Override
    public String toString() {
        return "[group=" + group + ", artifact=" + artifact + ']';
    }
}
