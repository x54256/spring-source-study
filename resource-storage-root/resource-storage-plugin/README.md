# rs

计算 hash 时间太长怎么办？

小文件耗时不会太多。大文件前端计算。

当返回值为空的时候，使用 map() 拿不到元素，要使用 switchIfEmpty() 来进一步操作

**nio 线程不能被阻塞**，如果需要阻塞，请在调用时创建一个新线程

```java
class A {

    @NotNull
    private Mono<String> download(String fileHash) {
        return Mono.create(sink -> {
            this.getReadyMetadata(fileHash)
                    .subscribe(m -> {
                        // 拼接本地缓存路径，格式：缓存目录/hashcode.tmp
                        String tempPath = LOCAL_TEMP_PATH + m.getFileHash() + SUFFIX;
                        if (FileUtil.exist(tempPath)) {
                            sink.success(tempPath);
                        } else {
                            log.info("tempPath：「{}」", tempPath);
                            // 2021/4/28 why????? 为啥要新开一个线程
                            // 假设当前代码运行的线程为 N2-2，我们进入 doDownload() 方法，里面有一个循环，也是使用 N2-2 线程发送两个请求
                            // （应该是做了判断，判断当前线程是不是 EventLoopGroup 中的线程，如果不是才会进行线程的切换），可能 mongo 内部
                            // 有一个机制就是请求线程与接收线程绑定，即第一个请求用 N2-2 接收，第二个请求用 N2-3 接收，因为我们 for 循环之后
                            // 调用了 latch.await(); 将 N2-2 阻塞住了，所以当消息来了之后 N2-2 无法接收，所以程序一直无法停止。
                            // 所以，我们不能让 nio 线程阻塞，那就需要在调用时重新创建一个线程了。
                            scheduler.schedule(() -> {
                                // 这块已经是子线程了，但还是最好不要用阻塞 api 吧
                                String block = this.doDownload(m, tempPath).block();
                                sink.success(tempPath);
                            });
                        }
                    });
        });
    }

    @NotNull
    private Mono<String> doDownload(FileMetadata metadata, String tempPath) {
        return Mono.create(sink -> {
            // 获取每一片的信息，排序
            List<FileMetadata.FsFilesInfo> fsFilesInfoList = metadata.getFsFilesInfoList();
            fsFilesInfoList.sort(Comparator.comparingInt(FileMetadata.FsFilesInfo::getChunk));

            long index = 0;
            CountDownLatch latch = new CountDownLatch(fsFilesInfoList.size());
            try {
                for (FileMetadata.FsFilesInfo fsFilesInfo : fsFilesInfoList) {
                    RandomAccessFile randomAccessFile = new RandomAccessFile(tempPath, "rw");
                    randomAccessFile.seek(index);
                    index += fsFilesInfo.getChunkSize();

                    // 开始下载
                    gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fsFilesInfo.getFsFilesId())))
                            .log()
                            .flatMap(gridFsTemplate::getResource)
                            .map(ReactiveGridFsResource::getDownloadStream)
                            .flux()
                            .flatMap(dataBufferFlux -> DataBufferUtils.write(dataBufferFlux, randomAccessFile.getChannel()))
                            .doOnError(sink::error)
                            .doOnComplete(() -> {
                                try {
                                    randomAccessFile.close();
                                } catch (IOException e) {
                                    sink.error(e);
                                }
                                latch.countDown();
                            }).subscribe();
                }

                // 注释掉，在调用方加个 sleep 就可以看到正常接收了 onNext()
                latch.await();
                sink.success(tempPath);
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }
}
```

