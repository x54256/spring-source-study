package cn.x5456.spel.study.expression;

/**
 * @author yujx
 * @date 2021/03/17 15:04
 */
public class ResolveExpressionException extends RuntimeException {

    public ResolveExpressionException(String message) {
        super(message);
    }

    public ResolveExpressionException(String message, Throwable cause) {
        super(message, cause);
    }
}
