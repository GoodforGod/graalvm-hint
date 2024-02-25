package io.goodforgod.graalvm.hint.processor;

import java.util.Objects;

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
        return (artifact == null)
                ? new HintFile(fileName, "META-INF/native-image/" + group)
                : new HintFile(fileName, "META-INF/native-image/" + group + "/" + artifact);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        HintOrigin that = (HintOrigin) o;
        return Objects.equals(group, that.group) && Objects.equals(artifact, that.artifact);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, artifact);
    }

    @Override
    public String toString() {
        return (artifact == null)
                ? "[group=" + group + ']'
                : "[group=" + group + ", artifact=" + artifact + ']';
    }
}
