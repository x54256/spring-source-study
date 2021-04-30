package cn.x5456.rs.pre;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.watch.SimpleWatcher;
import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.lang.Console;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

/**
 * @author yujx
 * @date 2021/04/30 20:40
 */
public class OtherTest {


    /*
    1. 有一定的延迟
    2. 如果启动的过程中创建了怎么办？
        - 一定的超时时间
        - 定时任务检测，当存在关闭监听
     */
//    @Test
    @Deprecated
    public void test() throws InterruptedException {
        File file = FileUtil.file("/private/var/folders/28/1tyh6prj3xg6xcdx_3qlkwr80000gn/T/cn.x5456.rs/8f16914a301e3d7855d856ad1924a4640b387f00536db72b123abc69552b37a9.pdf");
        //这里只监听文件或目录的修改事件
        WatchMonitor watchMonitor = WatchMonitor.create(file, WatchMonitor.ENTRY_CREATE);
        watchMonitor.setWatcher(new SimpleWatcher() {
            @Override
            public void onCreate(WatchEvent<?> event, Path currentPath) {
                Object obj = event.context();
                Console.log("创建：{}-> {}", currentPath, obj);
                watchMonitor.close();
            }
        });
        //启动监听
        watchMonitor.start();

        watchMonitor.join();
    }
}
