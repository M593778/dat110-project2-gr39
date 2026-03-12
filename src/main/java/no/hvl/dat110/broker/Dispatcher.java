package no.hvl.dat110.broker;

import java.util.Set;
import java.util.Collection;

import no.hvl.dat110.common.Logger;
import no.hvl.dat110.common.Stopable;
import no.hvl.dat110.messages.*;
import no.hvl.dat110.messagetransport.Connection;

public class Dispatcher extends Stopable {

	private Storage storage;

	public Dispatcher(Storage storage) {
		super("Dispatcher");
		this.storage = storage;
	}

	@Override
	public void doProcess() {

		Collection<ClientSession> clients = storage.getSessions();

		Logger.lg(".");
		for (ClientSession client : clients) {

			Message msg = null;

			if (client.hasData()) {
				msg = client.receive();
			}

			if (msg != null) {
				dispatch(client, msg);
			}
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void dispatch(ClientSession client, Message msg) {

		MessageType type = msg.getType();

		switch (type) {

		case DISCONNECT:
			onDisconnect((DisconnectMsg) msg);
			break;

		case CREATETOPIC:
			onCreateTopic((CreateTopicMsg) msg);
			break;

		case DELETETOPIC:
			onDeleteTopic((DeleteTopicMsg) msg);
			break;

		case SUBSCRIBE:
			onSubscribe((SubscribeMsg) msg);
			break;

		case UNSUBSCRIBE:
			onUnsubscribe((UnsubscribeMsg) msg);
			break;

		case PUBLISH:
			onPublish((PublishMsg) msg);
			break;

		default:
			Logger.log("broker dispatch - unhandled message type");
			break;
		}
	}

	// called from Broker after having established the underlying connection
	public void onConnect(ConnectMsg msg, Connection connection) {

		String user = msg.getUser();

		Logger.log("onConnect:" + msg.toString());

		storage.addClientSession(user, connection);
	}

	// called by dispatch upon receiving a disconnect message
	public void onDisconnect(DisconnectMsg msg) {

		String user = msg.getUser();

		Logger.log("onDisconnect:" + msg.toString());

		storage.removeClientSession(user);
	}

	public void onCreateTopic(CreateTopicMsg msg) {

		Logger.log("onCreateTopic:" + msg.toString());

		String topic = msg.getTopic();
		storage.createTopic(topic);
	}

	public void onDeleteTopic(DeleteTopicMsg msg) {

		Logger.log("onDeleteTopic:" + msg.toString());

		String topic = msg.getTopic();
		storage.deleteTopic(topic);
	}

	public void onSubscribe(SubscribeMsg msg) {

		Logger.log("onSubscribe:" + msg.toString());

		String user = msg.getUser();
		String topic = msg.getTopic();

		storage.addSubscriber(user, topic);
	}

	public void onUnsubscribe(UnsubscribeMsg msg) {

		Logger.log("onUnsubscribe:" + msg.toString());

		String user = msg.getUser();
		String topic = msg.getTopic();

		storage.removeSubscriber(user, topic);
	}

	public void onPublish(PublishMsg msg) {

		Logger.log("onPublish:" + msg.toString());

		String topic = msg.getTopic();
		Set<String> subscribers = storage.getSubscribers(topic);

		if (subscribers == null) {
			return;
		}

		for (String user : subscribers) {
			ClientSession session = storage.getSession(user);

			if (session != null) {
				session.send(msg);
			}
		}
	}
}
