package msg;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * The class attaches the counter to the message
 */
public class Msg {

    protected Msg() {
    }

    protected static JSONParser parser = new JSONParser();
    private String instruction;
    private int counter = 1;

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String toJString() {
        JSONObject obj = new JSONObject();
        obj.put("counter", counter);
        obj.put("instruction", instruction);
        return obj.toJSONString();
    }


}