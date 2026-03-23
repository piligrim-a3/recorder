package ftt.data;

import java.util.List;

public class Message {

    public static final int TYPE_STRING = 0;
    public static final int TYPE_NUMBER = 1;
    public static final int TYPE_LIST  = 2;

    private Object value;
    private int type;

    public Message(String message) {
        value = message;
        type = TYPE_STRING;
    }

    public Message(Number number) {
        value = number;
        type = TYPE_NUMBER;
    }

    public Message(List list) {
        value = list;
        type = TYPE_LIST;
    }
}
