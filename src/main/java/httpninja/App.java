package httpninja;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;

public class App {
	public static void main(String[] args) {
		HttpNinjaSpringBean bean = new HttpNinjaSpringBean();
		List<InetSocketAddress> proxies = new ArrayList<InetSocketAddress>();
		proxies.add(new InetSocketAddress("localhost", 9050));
		proxies.add(new InetSocketAddress("localhost", 9051));
		bean.setProxies(proxies);

		String[] urls = new String[] { "http://www.google.com",
				"http://www.ya.ru", "http://www.microsoft.com",
				"https://www.google.com" };
		Getter[] getters = new Getter[urls.length];

		App a = new App();

		for (int i = 0; i < urls.length; i++) {
			getters[i] = a.new Getter(urls[i], bean.getHttpConnection());
		}

		for (int i = 0; i < urls.length; i++) {
			getters[i].start();
		}

		for (int i = 0; i < urls.length; i++) {
			try {
				getters[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	class Getter extends Thread {
		String url;
		HttpConnection connection;

		public Getter(String url, HttpConnection connection) {
			this.url = url;
			this.connection = connection;
		}

		public void run() {
			Document d = connection.get(url);
			System.out.println(d.baseUri());
		}
	}
}
