package be.valuya.cestzam.client.debug;

import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Flow;

public class DebugBodySubscriber implements Flow.Subscriber<ByteBuffer> {

    private HttpResponse.BodySubscriber<String> wrapped;

    public DebugBodySubscriber(HttpResponse.BodySubscriber<String> wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        wrapped.onSubscribe(subscription);
    }

    @Override
    public void onNext(ByteBuffer item) {
        wrapped.onNext(List.of(item));
    }

    @Override
    public void onError(Throwable throwable) {
        wrapped.onError(throwable);
    }

    @Override
    public void onComplete() {
        wrapped.onComplete();
    }

}
