package mock.model;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

import rapid.cloud.cdn.socket.model.RapidPacket;


public class MockRapidPacket {
    private RapidPacket rapidPacket;
    private InetSocketAddress [] socketAddr;
    private DatagramChannel udpChannel = null;
    private SocketChannel tcpChannel = null;
    public MockRapidPacket(ByteBuffer buffer, DatagramChannel channel,
     InetSocketAddress [] socketAdrrs) {
        rapidPacket = new RapidPacket();
        rapidPacket.setByteBuffer(buffer);
        this.socketAddr = socketAdrrs;
        this.udpChannel = channel;
    }

    public DatagramChannel getUdpChannel() {
        return this.udpChannel;
    }

    public InetSocketAddress [] getSocketInetAddress() {
        return this.socketAddr;
    }

    public void setTcpChannel(SocketChannel channel) {
        this.tcpChannel = channel;
    }

    public SocketChannel getTcpChannel() {
        return this.tcpChannel;
    }

    public RapidPacket getRapidPacket() {
        return this.rapidPacket;
    }

    public void setRapidPacket(RapidPacket rapidPacket) {
        this.rapidPacket = rapidPacket;
    }

}
