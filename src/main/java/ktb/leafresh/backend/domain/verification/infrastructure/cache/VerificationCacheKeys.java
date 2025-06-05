package ktb.leafresh.backend.domain.verification.infrastructure.cache;

public class VerificationCacheKeys {

    public static String stat(Long verificationId) {
        return "verification:stat:" + verificationId;
    }

    public static String dirtySetKey() {
        return "verification:stat:dirty";
    }
}
