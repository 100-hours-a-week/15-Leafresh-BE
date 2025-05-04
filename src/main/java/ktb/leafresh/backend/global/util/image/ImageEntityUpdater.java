package ktb.leafresh.backend.global.util.image;

import jakarta.transaction.Transactional;
import ktb.leafresh.backend.global.common.entity.BaseEntity;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 이미지 관련 soft delete / 순서 변경 / 추가 로직을 공통 처리
 */
@Component
@RequiredArgsConstructor
public class ImageEntityUpdater {

    @Transactional
    public <E extends BaseEntity & ImageEntity, O> void update(
            O owner,
            List<KeepImage> keepList,
            List<E> newEntities,
            List<Long> deletedIds,
            JpaRepository<E, Long> repository
    ) {
        handleDeletes(deletedIds, repository);
        handleUpdates(keepList, repository);
        handleInserts(newEntities, repository);
    }

    private <E extends BaseEntity & ImageEntity> void handleDeletes(List<Long> deletedIds, JpaRepository<E, Long> repository) {
        if (deletedIds != null) {
            deletedIds.forEach(id -> repository.findById(id)
                    .ifPresent(BaseEntity::softDelete));
        }
    }

    private <E extends BaseEntity & ImageEntity> void handleUpdates(List<KeepImage> keepList, JpaRepository<E, Long> repository) {
        if (keepList != null) {
            for (KeepImage keep : keepList) {
                E entity = repository.findById(keep.id())
                        .orElseThrow(() -> new CustomException(ErrorCode.ACCESS_DENIED));
                entity.updateSequenceNumber(keep.sequenceNumber());
            }
        }
    }

    private <E extends BaseEntity & ImageEntity> void handleInserts(List<E> newEntities, JpaRepository<E, Long> repository) {
        if (newEntities != null && !newEntities.isEmpty()) {
            repository.saveAll(newEntities);
        }
    }

    public record KeepImage(Long id, int sequenceNumber) {}
}
