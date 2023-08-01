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
    AUTO_FALLBACK("--auto-fallback"),
    NO_FALLBACK("--no-fallback"),
    FORCE_FALLBACK("--force-fallback"),
    USE_GLIBC("--libc=glibc"),
    USE_MUSL("--libc=musl"),
    VERBOSE("--verbose"),
    VERSION("--version"),
    ENABLE_HTTP("--enable-http"),
    /**
     * <a href="https://www.graalvm.org/latest/reference-manual/native-image/optimizations-and-performance/MemoryManagement/#g1-garbage-collector">GC</a>
     */
    ENABLE_G1("-H:+UseLowLatencyGC"),
    ENABLE_SERIAL("--gc=serial"),
    ENABLE_HTTPS("--enable-https"),
    ENABLE_MONITORING("--enable-monitoring=jmxserver,jmxclient,jvmstat"),
    ENABLE_URL_PROTOCOLS("--enable-url-protocols"),
    ALLOW_INCOMPLETE_CLASSPATH("--allow-incomplete-classpath"),
    REPORT_UNSUPPORTED("--report-unsupported-elements-at-runtime"),
    QUICK_BUILD("-Ob"),
    MAX_COMPATIBILITY("-march=compatibility"),
    MAX_PERFORMANCE("-march=native"),
    BUILD_REPORT("-H:+BuildReport"),
    USE_LLVM("-H:CompilerBackend=llvm"),
    INLINE_BEFORE_ANALYSIS("-H:+InlineBeforeAnalysis"),
    PRINT_INITIALIZATION("-H:+PrintClassInitialization"),
    INCLUDE_ALL_LOCALES("-H:+IncludeAllLocales"),
    LOCALISATION_OPTIMIZED_MODE("-H:-LocalizationOptimizedMode"),
    LOG_REGISTERED_RESOURCE_MIN("-H:Log=registerResource:1"),
    LOG_REGISTERED_RESOURCE_MAX("-H:Log=registerResource:5");

    private final String option;

    NativeImageOptions(String option) {
        this.option = option;
    }

    public String option() {
        return option;
    }
}
