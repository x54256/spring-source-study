初步完成自定义表达式（@{}）、属性表达式（${}）、spel 表达式的解析（#{}），剩余优化工作如下：
1. ~~检测表达式是否正确（dubbo RpcResult、Tomcat 那个异常），直接返回这个对象，抛不抛异常由 Controller 决定~~
```
原始表达式
最终结果
是否有异常
异常对象
解析过程（包括错误）
```

2. Enum 工具类
3. re、like 的实现。匹配到多个怎么办？ reportMulti（模仿 spring）
4. ~~别名~~

![](https://tva1.sinaimg.cn/large/008eGmZEly1gooyk6uzakj31xa0oeq8q.jpg)

![](https://tva1.sinaimg.cn/large/008eGmZEly1gooymi1jx9j31bh0u049l.jpg)

![](https://tva1.sinaimg.cn/large/008eGmZEly1gooyoqr23sj316d0u0k2b.jpg)

5. ~~模仿 @Bean 注解，@Blueberry + @Config 注解，通过 beanpostprocess 或者 beanFactoryprocess 进行处理，到时候看看哪个合理一点~~

```java
// 处理 @Configuration 的类继承了 BeanDefinitionRegistryPostProcessor（它继承了 BFPostProcessor），所以感觉我们也可以用 BFPostProcessor 处理
// 或者看看 spring 给没给我们留钩子
public class ConfigurationClassPostProcessor implements BeanDefinitionRegistryPostProcessor,

```

![](https://tva1.sinaimg.cn/large/008eGmZEly1gop1pgvj81j32280tmtnl.jpg)

6. 别名和名称都有的同名问题，有点复杂，暂不解决

> 注：如果表达式的值是应该是 String 类型的，但是他也是一个数字，请带上''
>
> 例如：#{@{currRegionCode}.concat('JSYDGZQ.shx')} -> #{220512000000.concat('JSYDGZQ.shx')}，此时 SPEL 会将`220512000000`解析为 int 型，此时就会体现 SPEL 的两个问题：
> 1. 它为啥会将其解析为数字呢，为啥发现没有 concat 方法的时候不会再把它当做字符串重试一次呢。
> 2. [220512000000 超出了 int 能表示的大小，会出现解析失败](https://github.com/spring-projects/spring-framework/issues/20779)
>
> 所以此时只能改下我们的表达式 #{@{'currRegionCode'}.concat('JSYDGZQ.shx')}