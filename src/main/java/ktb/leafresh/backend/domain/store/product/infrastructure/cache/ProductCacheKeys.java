package ktb.leafresh.backend.domain.store.product.infrastructure.cache;

import java.util.Optional;

public class ProductCacheKeys {

    public static final String PRODUCT_SORTED_SET = "store:products:zset";
    public static final String TIMEDEAL_ZSET = "store:products:timedeal:zset";
    public static final String TIMEDEAL_ACTIVE = "store:products:timedeal:active";
    public static final String TIMEDEAL_LIST = "store:products:timedeal:list";

    private static final String START_CURSOR = "start";
    private static final String EMPTY_INPUT = "none";

    public static String single(Long productId) {
        return "store:products:single:" + productId;
    }

    public static String timedealSingle(Long policyId) {
        return "store:products:timedeal:single:" + policyId;
    }

    public static String productList(String input, Long cursorId, String cursorTimestamp, int size) {
        return String.format("store:products:list:%s:%s:%s:%d",
                Optional.ofNullable(input).orElse(EMPTY_INPUT),
                Optional.ofNullable(cursorId).map(String::valueOf).orElse(START_CURSOR),
                Optional.ofNullable(cursorTimestamp).orElse(START_CURSOR),
                size
        );
    }

    public static String productListFirstPage(String input, int size) {
        return String.format("store:products:list:first:%s:%d",
                Optional.ofNullable(input).orElse(EMPTY_INPUT),
                size
        );
    }

    public static String productIdListFirstPage(String input, int size) {
        return String.format("store:products:ids:first:%s:%d",
                Optional.ofNullable(input).orElse(EMPTY_INPUT),
                size
        );
    }
}
