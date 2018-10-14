package communication;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
	
	private int request;
    private byte[] content;
    private int senderId;

    public Message(int request, Object content, int senderId) {
        this.request = request;

        this.content = Serializer.serialize(content);
        //this.date = new Date();
        this.senderId = senderId;
    }

    public int getRequest() {
        return this.request;
    }

    public Object getContent() {
        return Serializer.deserialize(this.content);
    }

    public int getSender() {
        return this.senderId;
    }

}
