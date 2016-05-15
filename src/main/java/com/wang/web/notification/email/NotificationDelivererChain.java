package com.wang.web.notification.email;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.wang.web.notification.INotifiableExceptionDeliverer;

public class NotificationDelivererChain {
	protected @Autowired(required = false) List<INotifiableExceptionDeliverer> notificationDeliverers;

	public List<INotifiableExceptionDeliverer> getNotificationDeliverers() {
		return notificationDeliverers;
	}

	public void setNotificationDeliverers(List<INotifiableExceptionDeliverer> notificationDeliverers) {
		this.notificationDeliverers = notificationDeliverers;
	}
}
