package cn.x5456.rs.pre.cleanstrategy.redis.v2;

import cn.x5456.rs.pre.cleanstrategy.MongoTimeoutHolder;
import cn.x5456.rs.pre.document.FileMetadata;
import cn.x5456.rs.pre.document.FsFileTemp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MongoAfterSaveEventListener extends AbstractMongoEventListener<Object> {

    @Autowired
    private RedisCacheInfoRepo repo;

    @Override
    public void onAfterSave(AfterSaveEvent<Object> event) {
        Object source = event.getSource();
        if (source instanceof FileMetadata) {
            this.saveMetadataCache(((FileMetadata) source));
        } else if (source instanceof FsFileTemp) {
            this.saveTempCache(((FsFileTemp) source));
        }
    }

    private void saveTempCache(FsFileTemp fsFileTemp) {
        long ttl = MongoTimeoutHolder.getCleanTimeout(TimeUnit.SECONDS);
        RedisCacheInfo cacheInfo = RedisCacheInfo.builder()
                .id(fsFileTemp.getId())
                .fileHash(fsFileTemp.getFileHash())
                .isTemp(true)
                .ttl(ttl)
                .expirationOfNextMinute(roundUpToNextMinute(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(ttl)))
                .build();
        repo.save(cacheInfo);
        log.debug("FsFileTemp 的 cacheInfo 保存成功：「{}」", cacheInfo);
    }

    private void saveMetadataCache(FileMetadata fileMetadata) {
        // 分片上传的不进行主动清理 metadata
        if (fileMetadata.getMultipartUpload()) {
            return;
        }

        long ttl = MongoTimeoutHolder.getCleanTimeout(TimeUnit.SECONDS);
        RedisCacheInfo cacheInfo = RedisCacheInfo.builder()
                .id(fileMetadata.getId())
                .fileHash(fileMetadata.getFileHash())
                .isTemp(false)
                .ttl(ttl)
                .expirationOfNextMinute(roundUpToNextMinute(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(ttl)))
                .build();
        repo.save(cacheInfo);
        log.debug("FileMetadata 的 cacheInfo 保存成功：「{}」", cacheInfo);
    }

    private long roundUpToNextMinute(long timeInMs) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timeInMs);
        date.add(Calendar.MINUTE, 1);
        date.clear(Calendar.SECOND);
        date.clear(Calendar.MILLISECOND);
        return date.getTimeInMillis();
    }
}