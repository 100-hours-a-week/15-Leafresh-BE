package ktb.leafresh.backend.global.util.image;

/** 이미지 순서 변경 처리를 위한 공통 인터페이스 */
public interface ImageEntity {
  void updateSequenceNumber(int sequenceNumber);

  Long getId();
}
