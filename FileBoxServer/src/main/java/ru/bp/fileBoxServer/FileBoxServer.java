package ru.bp.fileBoxServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class FileBoxServer {
    private int maxObjectSize = 50 * 1024 * 1024;



    public static void main(String[] args) {
        new FileBoxServer().start();

    }

    public void run() throws Exception {
        EventLoopGroup mainGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(mainGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
                                    new ObjectDecoder(maxObjectSize, ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new MainHandler()
                            );
                        }
                    });
//                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = b.bind(PropertyReader.getInstance().getPort()).sync();
            future.channel().closeFuture().sync();
        } finally {
            mainGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void testSteps(){


        shutdown();

    }




    public void start() {
//        try {
//            MySQLService.start();
//        } catch (MySQLConnectException e) {
//            log.throwing(e);
//            log.warn("Остановка сервера");
//            shutdown();
//            System.exit(-1);
//        }
        try {
            run();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void shutdown() {
        log.trace("Остановка сервера..");
//        log.trace("Отключение клиентов..");
//        executorService.shutdownNow();
        log.trace("Остановка сервиса БД");
        MySQLService.stop();
        log.info("Server stopped");
    }

}
