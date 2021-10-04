package io.goodforgod.graalvm.hint.processor;

/**
 *
 *
 * @author Anton Kurako (GoodforGod)
 * @since 29.09.2021
 */
class HintOptions {

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

    public HintOptions(String group, String artifact) {
        this.group = group;
        this.artifact = artifact;
    }

    public String getGroup() {
        return group;
    }

    public String getArtifact() {
        return artifact;
    }

    public String getRelativePathForFile(String fileName) {
        return "META-INF/native-image/" + group + "/" + artifact + "/" + fileName;
    }

    @Override
    public String toString() {
        return "[group=" + group + ", artifact=" + artifact + ']';
    }
}
