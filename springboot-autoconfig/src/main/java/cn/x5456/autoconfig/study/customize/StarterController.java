package cn.x5456.autoconfig.study.customize;

public class StarterController {

    private String msg;

    public StarterController(String msg){
        this.msg=msg;
    }

    public void hello() {
        System.out.println(msg);
    }
}