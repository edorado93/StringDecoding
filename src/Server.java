import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.SystemPropertyUtil;

public class Server {
	
	private int port;
	private int BOSS_THREADS = 1;
	private int MAX_WORKER_THREADS = 16;

	public Server(int p)
	{
		System.out.println("I am constructor");
		port = p;
	}
	
	private int calculateThreadCount() 
	{
	    int threadCount;
	    if ((threadCount = SystemPropertyUtil.getInt("io.netty.eventLoopThreads", 0)) > 0) 
	    {
	        return threadCount;
	    }
	    
	    else 
	    {
	        threadCount = Runtime.getRuntime().availableProcessors() * 2;
	        return threadCount > MAX_WORKER_THREADS ? MAX_WORKER_THREADS : threadCount;
	    }
	}
	
	public void run() throws Exception {
		
		//System.setProperty("org.jboss.netty.epollBugWorkaround", "true");
		
		EventLoopGroup bossPool=new NioEventLoopGroup(BOSS_THREADS);
		EventLoopGroup workerPool=new NioEventLoopGroup(calculateThreadCount());
		
		try {
			
			ServerBootstrap boot=new ServerBootstrap();
			boot.group(bossPool,workerPool);
			boot.channel(NioServerSocketChannel.class);
			boot.childHandler(new Pipeline());
			boot.option(ChannelOption.TCP_NODELAY, true);
			boot.option(ChannelOption.SO_KEEPALIVE, true);
			boot.option(ChannelOption.SO_REUSEADDR, true);
			boot.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000);
			boot.option(ChannelOption.SO_BACKLOG, 1024);
			System.out.println("Server started listening at port : " + port);
			boot.bind(port).sync().channel().closeFuture().sync();
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally {			
			workerPool.shutdownGracefully();
			bossPool.shutdownGracefully();
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		
		
        new Server(8080).run();
	}
}
