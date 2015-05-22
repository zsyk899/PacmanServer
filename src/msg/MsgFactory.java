package msg;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Created by Benjamin on 15/5/9.
 */
public class MsgFactory {

    public JSONParser parser = new JSONParser();

    public Msg getNewInstance() {
        return new Msg();
    }

    public Msg fromJString(String JString) {
        JSONObject obj = null;
        try {
            obj = (JSONObject) parser.parse(JString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Msg msg = null;
        if (obj != null) {
            msg = new Msg();
            msg.setCounter(((Long)obj.get("counter")).intValue());
            msg.setInstruction((String) obj.get("instruction"));
        }
        return msg;
    }
}
