初步完成自定义表达式（@{}）、属性表达式（${}）、spel 表达式的解析（#{}），剩余优化工作如下：
1. 检测表达式是否正确（dubbo RpcResult、Tomcat 那个异常），直接返回这个对象，抛不抛异常由 Controller 决定
```
原始表达式
最终结果
是否有异常
异常对象
解析过程（包括错误）
```

2. Enum 工具类
3. re、like 的实现。匹配到多个怎么办？ reportMulti（模仿 spring）
4. 别名

![](https://tva1.sinaimg.cn/large/008eGmZEly1gooyk6uzakj31xa0oeq8q.jpg)

![](https://tva1.sinaimg.cn/large/008eGmZEly1gooymi1jx9j31bh0u049l.jpg)

![](https://tva1.sinaimg.cn/large/008eGmZEly1gooyoqr23sj316d0u0k2b.jpg)

5. 模仿 @Bean 注解，@Blueberry + @Config 注解，通过 beanpostprocess 或者 beanFactoryprocess 进行处理，到时候看看哪个合理一点

```java
// 处理 @Configuration 的类继承了 BeanDefinitionRegistryPostProcessor（它继承了 BFPostProcessor），所以感觉我们也可以用 BFPostProcessor 处理
// 或者看看 spring 给没给我们留钩子
public class ConfigurationClassPostProcessor implements BeanDefinitionRegistryPostProcessor,

```

BFR 和 AP 啥关系，看看我们这个合不合理