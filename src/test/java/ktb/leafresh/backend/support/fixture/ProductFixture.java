package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.domain.entity.enums.ProductStatus;

import java.util.ArrayList;

public class ProductFixture {

  private static final String DEFAULT_DESCRIPTION = "테스트 상품 설명";
  private static final String DEFAULT_IMAGE_URL = "https://dummy.image/product.png";

  public static Product create(String name, int price, int stock, ProductStatus status) {
    return Product.builder()
        .name(name)
        .description(DEFAULT_DESCRIPTION)
        .imageUrl(DEFAULT_IMAGE_URL)
        .price(price)
        .stock(stock)
        .status(status)
        .timedealPolicies(new ArrayList<>())
        .build();
  }

  public static Product createActiveProduct(String name, int price, int stock) {
    return create(name, price, stock, ProductStatus.ACTIVE);
  }

  public static Product createInactive(String name, int price, int stock) {
    return create(name, price, stock, ProductStatus.INACTIVE);
  }

  public static Product createDefaultProduct() {
    return createActiveProduct("기본 상품", 3000, 10);
  }
}
