
package messages.query;

import java.io.Serializable;

public class QueryTimeout implements Serializable {

    private static final long serialVersionUID = 8051921274321985002L;

    private static QueryTimeout Instance = null;

    private QueryTimeout() {}

    public static QueryTimeout getInstance() {
        if (Instance == null) {
            Instance = new QueryTimeout();
        }
        return Instance;
    }

}
