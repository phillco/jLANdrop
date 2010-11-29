package landrop.peering;
import java.util.Queue;

public interface PeerEventListener
{
	public void peerListUpdated( Queue<Peer> peers );

}
