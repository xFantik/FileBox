import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import messages.FileMessage;
import messages.FileRequest;

import java.nio.file.Files;
import java.nio.file.Paths;

public class MainHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FileRequest) {
            FileRequest fr = (FileRequest) msg;
            if (Files.exists(Paths.get("server_storage/" + fr.getFilename()))) {
                FileMessage fm = new FileMessage(Paths.get("server_storage/" + fr.getFilename()));
                ctx.writeAndFlush(fm);
            }
        }


        if (msg instanceof  FileMessage){
            FileMessage fm = (FileMessage) msg;

            System.out.print("Обработка fileMessage: ");

            System.out.println(fm.getFilename());
            //todo найти id в sql

            //todo тело файла

            //todo проверить хэш

            //todo обновить статус в sql
        }


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
