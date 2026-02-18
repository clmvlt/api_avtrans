package bzh.stack.apiavtrans.repository;

import bzh.stack.apiavtrans.entity.AppVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppVersionRepository extends JpaRepository<AppVersion, UUID> {

    List<AppVersion> findByIsActiveTrueOrderByVersionCodeDesc();

    List<AppVersion> findAllByOrderByVersionCodeDesc();

    Optional<AppVersion> findTopByIsActiveTrueOrderByVersionCodeDesc();

    Optional<AppVersion> findByVersionCode(Integer versionCode);

    boolean existsByVersionCode(Integer versionCode);

    @Modifying
    @Query("UPDATE AppVersion a SET a.downloadCount = a.downloadCount + 1 WHERE a.id = :id")
    void incrementDownloadCount(@Param("id") UUID id);
}
