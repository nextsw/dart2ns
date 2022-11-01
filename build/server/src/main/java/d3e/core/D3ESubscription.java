package d3e.core;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.flowables.ConnectableFlowable;
import javax.annotation.PostConstruct;
import models.AnonymousUser;
import models.BaseUser;
import models.BaseUserSession;
import models.OneTimePassword;
import models.VerificationData;
import org.springframework.scheduling.annotation.Async;
import store.DataStoreEvent;

@org.springframework.stereotype.Service
public class D3ESubscription implements FlowableOnSubscribe<DataStoreEvent> {
  public ConnectableFlowable<DataStoreEvent> flowable;
  private FlowableEmitter<DataStoreEvent> emitter;

  @PostConstruct
  public void init() {
    this.flowable = Flowable.create(this, BackpressureStrategy.BUFFER).publish();
    this.flowable.connect();
    flowable.subscribe((a) -> {});
  }

  @Async
  public void handleContextStart(DataStoreEvent event) {
    this.emitter.onNext(event);
  }

  @Override
  public void subscribe(FlowableEmitter<DataStoreEvent> emitter) throws Throwable {
    this.emitter = emitter;
  }

  public Flowable<D3ESubscriptionEvent<AnonymousUser>> onAnonymousUserChangeEvent() {
    return this.flowable
        .filter((e) -> e.getEntity() instanceof AnonymousUser)
        .map(
            (e) -> {
              D3ESubscriptionEvent<AnonymousUser> event = new D3ESubscriptionEvent<>();
              event.model = ((AnonymousUser) e.getEntity());
              event.changeType = e.getType();
              return event;
            });
  }

  public Flowable<D3ESubscriptionEvent<BaseUser>> onBaseUserChangeEvent() {
    return this.flowable
        .filter((e) -> e.getEntity() instanceof BaseUser)
        .map(
            (e) -> {
              D3ESubscriptionEvent<BaseUser> event = new D3ESubscriptionEvent<>();
              event.model = ((BaseUser) e.getEntity());
              event.changeType = e.getType();
              return event;
            });
  }

  public Flowable<D3ESubscriptionEvent<BaseUserSession>> onBaseUserSessionChangeEvent() {
    return this.flowable
        .filter((e) -> e.getEntity() instanceof BaseUserSession)
        .map(
            (e) -> {
              D3ESubscriptionEvent<BaseUserSession> event = new D3ESubscriptionEvent<>();
              event.model = ((BaseUserSession) e.getEntity());
              event.changeType = e.getType();
              return event;
            });
  }

  public Flowable<D3ESubscriptionEvent<OneTimePassword>> onOneTimePasswordChangeEvent() {
    return this.flowable
        .filter((e) -> e.getEntity() instanceof OneTimePassword)
        .map(
            (e) -> {
              D3ESubscriptionEvent<OneTimePassword> event = new D3ESubscriptionEvent<>();
              event.model = ((OneTimePassword) e.getEntity());
              event.changeType = e.getType();
              return event;
            });
  }

  public Flowable<D3ESubscriptionEvent<VerificationData>> onVerificationDataChangeEvent() {
    return this.flowable
        .filter((e) -> e.getEntity() instanceof VerificationData)
        .map(
            (e) -> {
              D3ESubscriptionEvent<VerificationData> event = new D3ESubscriptionEvent<>();
              event.model = ((VerificationData) e.getEntity());
              event.changeType = e.getType();
              return event;
            });
  }
}
