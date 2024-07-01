package server;

import java.util.concurrent.ConcurrentHashMap;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * ScheduledTask class
 */
public class ScheduledTask implements Runnable {
    private SearchEngine searchEngine = null;
    private ReviewEngine reviewEngine = null;
    private ConcurrentHashMap<String, Hotel> oldHotelsMap = null;
    private int udpPort = 0;
    private String udpIp = "";

    /**
     * Constructor
     * 
     * @param hotelsPath the path of the hotels file
     * @param udpPort    the UDP port
     * @param udpIp      the UDP IP
     */
    public ScheduledTask(String hotelsPath, int udpPort, String udpIp) {
        this.searchEngine = new SearchEngine(hotelsPath);
        this.reviewEngine = new ReviewEngine(hotelsPath);
        this.oldHotelsMap = new ConcurrentHashMap<String, Hotel>();
        this.udpPort = udpPort;
        this.udpIp = udpIp;
    }

    /**
     * Run the task
     */
    public void run() {
        try {
            reviewEngine.updateHotelFile(reviewEngine.calculateMeanRatesById());
            ConcurrentHashMap<String, Hotel> newHotelsMap = searchEngine.getBestHotelsMap();
            String toSend = searchEngine.getChangedHotelsString(oldHotelsMap, newHotelsMap);

            byte[] message = toSend.getBytes();
            InetAddress multicastAddress = InetAddress.getByName(this.udpIp);
            DatagramPacket packet = new DatagramPacket(message, message.length, multicastAddress, udpPort);

            try (MulticastSocket socket = new MulticastSocket()) {
                socket.joinGroup(multicastAddress);
                socket.send(packet);
                System.out.println("[" + Thread.currentThread().getName() + "] - Messaggio UDP inviato in multicast");
                socket.leaveGroup(multicastAddress);
            }

            oldHotelsMap = newHotelsMap;
        } catch (Exception e) {
            System.out.println("Errore: " + e);
        }
    }

}
