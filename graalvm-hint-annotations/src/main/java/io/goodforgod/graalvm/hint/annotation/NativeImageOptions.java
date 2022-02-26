package io.goodforgod.graalvm.hint.annotation;

/**
 * Common native image options that can be used in {@link NativeImageHint#options()}
 *
 * @author Anton Kurako (GoodforGod)
 * @see <a href="https://www.graalvm.org/reference-manual/native-image/Options/">Native Image
 *          Options</a>
 * @since 15.10.2021
 */
public enum NativeImageOptions {

    DRY_RUN("--dry-run"),
    NATIVE_IMAGE_INFO("--native-image-info"),
    TRACE_CLASS_INIT("--trace-class-initialization"),
    TRACE_OBJECT_INIT("--trace-object-instantiation"),
    INLINE_BEFORE_ANALYSIS("-H:+InlineBeforeAnalysis"),
    PRINT_INITIALIZATION("-H:+PrintClassInitialization"),
    AUTO_FALLBACK("--auto-fallback"),
    NO_FALLBACK("--no-fallback"),
    FORCE_FALLBACK("--force-fallback"),
    USE_GLIBC("--libc=glibc"),
    USE_MUSL("--libc=musl"),
    VERBOSE("--verbose"),
    VERSION("--version"),
    ENABLE_HTTP("--enable-http"),
    ENABLE_HTTPS("--enable-https"),
    ENABLE_URL_PROTOCOLS("--enable-url-protocols"),
    ALLOW_INCOMPLETE_CLASSPATH("--allow-incomplete-classpath"),
    REPORT_UNSUPPORTED("--report-unsupported-elements-at-runtime");

    private final String option;

    NativeImageOptions(String option) {
        this.option = option;
    }

    public String option() {
        return option;
    }
}
