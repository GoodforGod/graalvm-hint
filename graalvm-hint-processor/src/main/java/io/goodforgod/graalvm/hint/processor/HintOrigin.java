package io.goodforgod.graalvm.hint.processor;

/**
 * Hint origin package and artifact
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.09.2021
 */
final class HintOrigin {

    public static final String HINT_PROCESSING_GROUP = "graalvm.hint.group";
    public static final String HINT_PROCESSING_ARTIFACT = "graalvm.hint.artifact";

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

    public String getRelativePathForFile(String fileName) {
        return "META-INF/native-image/" + group + "/" + artifact + "/" + fileName;
    }

    @Override
    public String toString() {
        return "[group=" + group + ", artifact=" + artifact + ']';
    }
}