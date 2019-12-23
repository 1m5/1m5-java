package io.onemfive.data;

import java.util.List;

/**
 * Created by Brian on 3/27/18.
 */
public interface Message extends JSONSerializable {
    void addErrorMessage(String errorMessage);
    List<String> getErrorMessages();
    void clearErrorMessages();
}
