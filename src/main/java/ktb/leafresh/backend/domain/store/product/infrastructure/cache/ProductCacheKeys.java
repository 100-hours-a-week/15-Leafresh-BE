package ktb.leafresh.backend.domain.store.product.infrastructure.cache;

public class ProductCacheKeys {

  public static final String PRODUCT_SORTED_SET = "store:products:zset";
  public static final String TIMEDEAL_ZSET = "store:products:timedeal:zset";
  public static final String TIMEDEAL_ACTIVE = "store:products:timedeal:active";
  public static final String TIMEDEAL_LIST = "store:products:timedeal:list";

  public static String single(Long productId) {
    return "store:products:single:" + productId;
  }

  public static String productStock(Long productId) {
    return "stock:product:" + productId;
  }

  public static String timedealStock(Long timedealPolicyId) {
    return "stock:timedeal:" + timedealPolicyId;
  }

  public static String timedealSingle(Long policyId) {
    return "store:products:timedeal:single:" + policyId;
  }
}
