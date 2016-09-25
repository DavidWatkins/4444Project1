package pentos.sim;

import java.io.EOFException;
import java.io.UnsupportedEncodingException;

public interface Sequencer {

    public void init();
    public Building next() throws EOFException, UnsupportedEncodingException;

}
