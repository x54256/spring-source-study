package cn.x5456.spel.study.expression;

import cn.hutool.core.util.StrUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yujx
 * @date 2021/03/19 12:47
 */
public class ResolveExpressionTrace implements Serializable {

    public final AtomicInteger stepNum = new AtomicInteger(0);

    // 初始的表达式
    private final String placeholder;

    // 解析的过程
    private final List<String> processes = new ArrayList<>();

    // 解析之后最终结果
    private String result;

    // 是否有异常
    private Boolean hasException = false;

    // 解析过程中遇到的异常信息
    private String errorMessage;

    // 异常信息
    private Exception exception;

    public ResolveExpressionTrace(String placeholder) {
        this.placeholder = placeholder;
        this.result = placeholder;
        this.processes.add(StrUtil.format("初始表达式为：「{}」", placeholder));
    }

    public void addProcess(String value, StringExpressionResolver resolver) {
        this.result = value;
        this.processes.add(StrUtil.format("第「{}」步解析表达式「{}{}」，解析后的结果为：「{}」",
                stepNum.incrementAndGet(), resolver.getPlaceholderPrefix(), resolver.getPlaceholderSuffix(), value));
    }

    public void recordErrorMsg(Exception exception) {
        this.hasException = true;
        String msg = StrUtil.format("解析表达式「{}」时出现错误，错误类型为「{}」，错误内容为「{}」！", placeholder, exception.getClass().getSimpleName(), exception.getMessage());
        this.processes.add(msg);
        this.errorMessage = msg;
        this.exception = exception;
    }

    public String getValue() {
        return result;
    }

    public ResolveExpressionTraceException getException() {
        String msg = StrUtil.format("{}\n解析的过程如下：\n{}", errorMessage, String.join(System.lineSeparator(), processes));
        return new ResolveExpressionTraceException(msg, exception);
    }

    public boolean hasException() {
        return hasException;
    }

    /**
     * @return 如果有异常抛出异常，没有则返回值
     */
    public String recreate() {
        if (hasException) {
            throw this.getException();
        }
        return result;
    }

    public List<String> getProcesses() {
        return processes;
    }

    @Override
    public String toString() {
        return this.recreate();
    }
}
