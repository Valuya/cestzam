package be.valuya.cestzam.myminfin.sync;

public class MyminfinSyncException extends RuntimeException {
    public MyminfinSyncException() {
    }

    public MyminfinSyncException(String message) {
        super(message);
    }

    public MyminfinSyncException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyminfinSyncException(Throwable cause) {
        super(cause);
    }

    public MyminfinSyncException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
