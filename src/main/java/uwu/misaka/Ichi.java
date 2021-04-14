package uwu.misaka;

import arc.Core;
import arc.func.Cons;
import arc.func.Prov;
import arc.util.Time;
import arc.util.async.AsyncExecutor;
import mindustry.net.Host;
import mindustry.net.NetworkIO;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class Ichi {
    static final AsyncExecutor executor = new AsyncExecutor(Math.max(Runtime.getRuntime().availableProcessors(), 6));
    static final Prov<DatagramPacket> packetSupplier = () -> new DatagramPacket(new byte[512], 512);
    public static void main(String[] a){
        System.out.println("START PINGING");
        new pingableHost("easyplay.su",6567).ping();
        new pingableHost("obvilionnetwork.ru",6567).ping();
        System.out.println("END PINHING");
    }
    public static class pingableHost{
        public String ip;
        public int port;
        public String succesMessage;
        public String dropMessage;
        public boolean booted;

        public pingableHost(String ip,int p){
            this.ip=ip;
            this.port = p;
            this.succesMessage = "meme";
            this.dropMessage = "clown";
            booted=true;
        }

        public void ping(){
            pingHost(ip, port, host -> {
                System.out.println("+ "+ip);
            }, e -> {
                System.out.println("- "+ip);
            });
        }
    }
    public static void pingHost(String address, int port, Cons<Host> valid, Cons<Exception> invalid){
        executor.submit(() -> {
            try{
                DatagramSocket socket = new DatagramSocket();
                long time = Time.millis();
                socket.send(new DatagramPacket(new byte[]{-2, 1}, 2, InetAddress.getByName(address), port));
                socket.setSoTimeout(2000);

                DatagramPacket packet = packetSupplier.get();
                socket.receive(packet);

                ByteBuffer buffer = ByteBuffer.wrap(packet.getData());
                Host host = NetworkIO.readServerData((int)Time.timeSinceMillis(time), packet.getAddress().getHostAddress(), buffer);

                Core.app.post(() -> valid.get(host));
            }catch(Exception e){
                Core.app.post(() -> invalid.get(e));
            }
        });
    }
}
