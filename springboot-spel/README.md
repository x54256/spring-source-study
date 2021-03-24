初步完成自定义表达式（@{}）、属性表达式（${}）、spel 表达式的解析（#{}），剩余优化工作如下：
1. ~~检测表达式是否正确（dubbo RpcResult、Tomcat 那个异常），直接返回这个对象，抛不抛异常由 Controller 决定~~
```
原始表达式
最终结果
是否有异常
异常对象
解析过程（包括错误）
```

2. ~~Enum 工具类~~
3. re、like 的实现。匹配到多个怎么办？ reportMulti（模仿 springboot）
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
> 1. 它为啥会将其解析为数字呢，为啥发现没有 concat 方法的时候不会再把它当做字符串重试一次呢。 所以此时只能改下我们的表达式 #{@{'currRegionCode'}.concat('JSYDGZQ.shx')}
> 2. [220512000000 超出了 int 能表示的大小，会出现解析失败](https://github.com/spring-projects/spring-framework/issues/20779)


---

设计思路：

@{}     自定义的表达式
${}     从配置文件中取
#{}     spel

> 注：如果文件名使用到了 @{、${、#{、*{ 怎么办，只要他不闭合（没有}）那么无所谓，否则的话 Spring 会报错，那我们也报错吧

目标是生成这样的字符串：360700JSYDGZQ.shx

#{@{currentRegionCode}.toString().substring(0, 4).concat(00)}${JSYDGZQ:JSYDGZQ}.shx

检索方式：用什么样的方式来确定采用哪种检索方式，一个一个试还是有一个标识。
1. =
2. re   -> 数据库支持正则吗
3. like

模糊查询，例如城市的首字母

1. @{} 生成多个表达式，ZQ、Z_、__
2. 正则或者让他用 like 匹配


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
5. 模仿 @Bean 注解，@Blueberry + @Config 注解，通过 beanpostprocess 或者 beanFactoryprocess 进行处理，到时候看看哪个合理一点

---

我的手机  22:25:34
看看aopcontext是怎么把当前对象放进去的
我的手机  22:26:08
看看能不能把reviewtaskid放进一个地方
我的手机  22:29:03
注解改为order接口
我的手机  22:29:45
#{} anymatch
我的手机  22:32:52
三种查询方式和表达式一起放进附件这个json中
我的手机  23:08:24
测试表达式的时候记录替换过程和报错信息
我的手机  23:08:40
可以模仿tomcat的那个异常
我的手机  23:09:01
和rocresult
我的手机  23:22:46
没必要value一定是lamda，也可以是一个对象，参数是landa和默认值
我的手机  23:23:45
相同类型的在同一个类中，其继承一个注册的接口，模仿beanfactoryregister
我的手机  23:25:09
那这样的话，那就要把自定义表达式当做一个工厂了，最好不要直接写在@{}那个类中
我的手机  23:25:28
用委托的方式，让sping注入进来
我的手机  23:27:15
表达式可以加别名
我的手机  23:28:01
模仿beandefi
我的手机  23:42:36
region那个return的字符串变成这个#{330000.tostring()}
我的手机  23:43:19
因为行政代码确实应该是string类型的
我的手机  07:12:03
3种寻找方式，接口 枚举 内部类
我的电脑  10:36:19
写下单元测试
我的手机  11:56:16
昨天的几个todo
我的手机  19:35:30
[文件]
我的手机  19:36:09
可以增加一个*{}模仿django的这个过滤器
我的手机  19:37:13
来执行一些自定义的操作，
我的手机  19:37:51
来对数据进行操作，比如四舍五入
我的手机  19:46:41
找一个业务场景，例如prs计算汇总表的数据。
我的手机  19:54:23
因为无法确定*#的先后顺序，所以他们的解析器中都需要包含一个组合的解析器，之前我们取出表达式之后是递归调用
我的手机  19:54:33
本方法
我的手机  19:54:49
这两个就需要改成调用组合类的方法。