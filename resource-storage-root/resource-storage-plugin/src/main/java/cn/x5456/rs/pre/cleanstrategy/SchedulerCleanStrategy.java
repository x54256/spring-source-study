package cn.x5456.rs.pre.cleanstrategy;

import cn.x5456.rs.pre.def.UploadProgress;
import cn.x5456.rs.pre.document.FileMetadata;
import cn.x5456.rs.pre.document.FsFileTemp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 定时清理未上传完成的文件缓存
 *
 * @author yujx
 * @date 2021/04/30 10:09
 */
@Slf4j
//@Component
public class SchedulerCleanStrategy extends AbstractMongoCleanStrategy {

    private Scheduler scheduler;

    @Autowired
    public void setScheduler(ObjectProvider<Scheduler> schedulerObjectProvider) {
        this.scheduler = schedulerObjectProvider.getIfUnique(Schedulers::elastic);
        // TODO: 2021/4/30 改为外部调用
        this.start();
    }

    @Override
    public void start() {
        AtomicLong n = new AtomicLong(1);
        scheduler.schedulePeriodically(() -> {

            log.info("正在执行清理操作，第「{}」次。", n.getAndIncrement());

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime dateTime = now.plusNanos(super.getCleanTimeout(TimeUnit.NANOSECONDS) * -1);

            this.cleanFsFileTemp(dateTime);
            this.cleanFileMetadata(dateTime);

        }, 1000, super.getConnectTimeoutMS() * 5L, TimeUnit.MILLISECONDS);
    }

    private void cleanFsFileTemp(LocalDateTime dateTime) {
        // 清理 FsFileTemp
        Criteria criteria = Criteria.where(FsFileTemp.UPLOAD_PROGRESS).is(UploadProgress.UPLOADING)
                .and(FsFileTemp.CREAT_TIME).lte(dateTime);
        mongoTemplate.find(Query.query(criteria), FsFileTemp.class)
                .groupBy(FsFileTemp::getFileHash)
                .subscribe(groupedFlux -> {

                    // 获取 hash
                    String hash = groupedFlux.key();

                    // 清理元数据表，如果 temp 表中还有已经上传完成的就不删除 metadata
                    Criteria c1 = Criteria.where(FsFileTemp.UPLOAD_PROGRESS).is(UploadProgress.UPLOAD_COMPLETED)
                            .and(FsFileTemp.FILE_HASH).is(hash);
                    // TODO: 2021/4/30 我觉得想要在空流里做点事真费劲，暂时用 block() 了。
                    FsFileTemp block = mongoTemplate.findOne(Query.query(c1), FsFileTemp.class).block();
                    if (block == null) {
                        Criteria c = Criteria.where(FileMetadata.FILE_HASH).is(hash)
                                .and(FileMetadata.UPLOAD_PROGRESS).is(UploadProgress.UPLOADING)
                                .and(FileMetadata.MULTIPART_UPLOAD).is(true)
                                .and(FileMetadata.CREAT_TIME).lte(dateTime);
                        mongoTemplate.findOne(Query.query(c), FileMetadata.class)
                                .subscribe(metadata -> {
                                    log.info("metadata：「{}」", metadata);
                                    mongoTemplate.remove(metadata).subscribe();
                                });
                    }

                    // 清理缓存表
                    groupedFlux.subscribe(temp -> {
                        log.info("temp：「{}」", temp);
                        mongoTemplate.remove(temp).subscribe();
                    });
                });
    }

    private void cleanFileMetadata(LocalDateTime dateTime) {
        // 清理 FsFileTemp
        Criteria criteria = Criteria.where(FileMetadata.UPLOAD_PROGRESS).is(UploadProgress.UPLOADING)
                .and(FileMetadata.MULTIPART_UPLOAD).is(false)
                .and(FileMetadata.CREAT_TIME).lte(dateTime);

        mongoTemplate.find(Query.query(criteria), FsFileTemp.class)
                .subscribe(metadata -> {
                    log.info("metadata：「{}」", metadata);
                    mongoTemplate.remove(metadata).subscribe();
                });
    }
}
