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

import java.io.*;

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
            ChannelFuture future = b.bind(8189).sync();
            future.channel().closeFuture().sync();
        } finally {
            mainGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void testSteps(){

        //
//        deserializeFromFile();


//        mySQLService.compareFileList("fant", fileHeadersList);




        //todo:            ЗАПИСЬ Тела файла в БЛОБ
//        try (InputStream is = Files.newInputStream(Paths.get(".\\pom.xml"))) {
//            mySQLService.addFileContent("fant", ".\\pom.xml", is);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        todo:              ПОЛУЧЕНИЕ тела файла
//        InputStream is = mySQLService.getFileContent("fant", ".\\pom.xml");
//        if (is!=null) {
//            int b;
//            try {
//                while ((b = is.read()) != -1) {
//                    System.out.print((char) b);
//
//                }
//                System.out.println();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }


//     todo:               Установка времени изменения файла

//        FileTime t = FileTime.from(1650972661, TimeUnit.SECONDS);
//        Files.setLastModifiedTime(new File(".\\pom.xml").toPath(),  t);


        shutdown();

    }




    public void start() {
        try {
            MySQLService.start();
        } catch (MySQLConnectException e) {
            log.throwing(e);
            log.warn("Остановка сервера");
            shutdown();
            System.exit(-1);
        }
        try {
            run();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
//
//    private void deserializeFromFile() {
//        try (var ois = new ObjectInputStream(new FileInputStream("filelist"))) {
//            fileHeadersList = (FileHeadersList) ois.readObject();
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//    }

    private void shutdown() {
        log.trace("Остановка сервера..");
//        log.trace("Отключение клиентов..");
//        executorService.shutdownNow();
        log.trace("Остановка сервиса БД");
        MySQLService.stop();
        log.info("Server stopped");
    }

}
