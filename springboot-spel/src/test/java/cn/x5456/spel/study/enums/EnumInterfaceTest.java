package cn.x5456.spel.study.enums;

import org.junit.Test;

/**
 * @author yujx
 * @date 2021/03/19 16:20
 */
public class EnumInterfaceTest {

    @Test
    public void test() {
        System.out.println(EnumInterface.get(SEASON.class, 0));
        System.out.println(EnumInterface.getByDesc(SEASON.class, "夏天"));
        System.out.println(EnumInterface.isLegal(SEASON.class, 0));
        System.out.println(EnumInterface.get(SEASON.class, 1));
        System.out.println(EnumInterface.getByDesc(SEASON.class, "12313123"));
        System.out.println(EnumInterface.isLegal(SEASON.class, 1));
    }

    enum SEASON implements EnumInterface {

        SUMMER(0, "夏天");

        int code;
        String desc;

        SEASON(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        @Override
        public int code() {
            return this.code;
        }

        @Override
        public String desc() {
            return this.desc;
        }
    }

}