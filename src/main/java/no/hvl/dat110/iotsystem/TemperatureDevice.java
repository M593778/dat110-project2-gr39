package no.hvl.dat110.iotsystem;

import no.hvl.dat110.client.Client;

public class TemperatureDevice {

	private static final int COUNT = 10;

	public static void main(String[] args) {

		// simulated / virtual temperature sensor
		TemperatureSensor sn = new TemperatureSensor();

		Client client = new Client("sensor", Common.BROKERHOST, Common.BROKERPORT);

		boolean connected = client.connect();

		if (connected) {
			System.out.println("temperature device started");

			for (int i = 0; i < COUNT; i++) {
				int temp = sn.read();
				client.publish(Common.TEMPTOPIC, Integer.toString(temp));

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			client.disconnect();
		}

		System.out.println("Temperature device stopping ... ");
	}
}
