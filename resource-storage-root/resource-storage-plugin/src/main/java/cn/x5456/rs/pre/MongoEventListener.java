package cn.x5456.rs.pre;

import cn.x5456.rs.pre.document.FileMetadata;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;

/**
 * @author yujx
 * @date 2021/04/29 17:01
 */
public class MongoEventListener extends AbstractMongoEventListener<FileMetadata> {

    /*
    方案一：定时任务版（一次任务的时间间隔不好控制，每次间隔随机时间看看好不好实现），不用结合 AbstractMongoEventListener

    当保存之后调用回调，创建一个延迟任务，检查这个文件是否上传完成，如果还没上传完就删除； -> 感觉没必要
    而且，假如当前服务器挂了，那么这个文件的元数据永远不会删，所以还是要用定时任务。

    定时任务就很简单了，定时去 metadata 表查询还没有上传完成并且时间超过了 mongo 超时时间两倍（这个参数可以通过这个 listener 设置）的进行删除。


    方案二：redis 过期键版（发布-订阅），结合 AbstractMongoEventListener

    需要引入 spring-data-redis，当保存之后调用回调，创建一个具有过期时间的 key，过期后调用回调进行删除
    问题：是否所有实例都会收到这个监听呢。

    方案三：MQ 版（最好，但一般项目不会用 mq），结合 AbstractMongoEventListener

    和 redis 版本大同小异，可以交给陆俊翔写。


    适配器模式呗 或 条件配置类（感觉这个靠谱）

    问题：

    第一个请求在上传 hash 值为 123abc 的文件，此时他在 metadata 表的状态是上传中。
    第二个请求也上传 hash 值为 123abc 的文件，因为 metadata 表中已经存在了，所以在 resource 表建立了引用，实现了秒传。
    但是第一个请求上传失败了，metadata 表数据删除了，怎么办！

    1. 如果发现已经有了一个 hash 值为 123abc 的文件在上传中，则阻塞住，等他上传完之后再建立引用。
    2. 新增一张 hash 冲突表，也进行上传，上传完成之后，如果：
        1）第一个请求上传成功，删除我们上传的文件（异步），建立引用
        2）如果第一个请求还在上传中状态，怎么办呢~~ -> 阻塞住，等待自动清理完成之后达成 3）的条件
            todo 测试下，同一个应用 t1 轮训，t2 删除是否能感知到。
        3）如果数据没了，直接把我们的转正到 metadata 表中，建立引用

        4）如果我们上传中，第一个请求上传完成了怎么办？
     */



    /**
     * Captures {@link AfterSaveEvent}.
     *
     * @param event will never be {@literal null}.
     * @since 1.8
     */
    @Override
    public void onAfterSave(AfterSaveEvent<FileMetadata> event) {
        super.onAfterSave(event);
    }
}
