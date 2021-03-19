package cn.x5456.spel.study.expression;

/**
 * @author yujx
 * @date 2021/03/19 15:49
 */
public class ResolveExpressionTraceException extends RuntimeException {

    public ResolveExpressionTraceException(String message) {
        super(message);
    }

    public ResolveExpressionTraceException(String message, Throwable cause) {
        super(message, cause);
    }
}
