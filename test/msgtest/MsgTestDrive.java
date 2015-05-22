package msgtest;


import msg.*;

/**
 * Created by Benjamin on 15/5/9.
 */
public class MsgTestDrive {
    public static void main(String[] args) {
        MsgFactory msgFactory = new MsgFactory();
        Msg msgToJString = msgFactory.getNewInstance();
        msgToJString.setCounter(2);
        msgToJString.setInstruction("test");
        String jst = msgToJString.toJString();
        Msg msgFromJString = msgFactory.fromJString(jst);
        System.out.println(jst);
        System.out.println(msgFromJString.toJString());
    }
}
