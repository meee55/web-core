package net.tinybrick.web.notification;

public interface INotifiableExceptionDeliverer {
	public boolean enabled();

	public void sendNotification(Throwable ex, String name) throws Throwable;
}
