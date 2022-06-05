import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
public class MainHandler extends ChannelInboundHandlerAdapter {
    ExecutorService executorService;
    private int waitingFileCountInList;
    private String userName = "fant";

    private List<FileHeader> filesHeadersListOnClient;
    private List<FileHeader> filesHeadersListOnServer;

    public MainHandler() {
        System.out.println("СОЗДАН Экзекьютор");
        this.executorService = Executors.newSingleThreadExecutor();
        filesHeadersListOnServer = MySQLService.getDataBaseFileList(userName);
        filesHeadersListOnClient = new ArrayList<>();

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FileRequest) {
            FileRequest fr = (FileRequest) msg;
            if (Files.exists(Paths.get("server_storage/", userName, fr.getFilename()))) {
                FileMessage fm = new FileMessage(Paths.get("server_storage/", userName, fr.getFilename()));
                ctx.writeAndFlush(fm);
            }
        }



        if (msg instanceof InfoMessage) {
            InfoMessage.MessageType action = ((InfoMessage) msg).getMessageType();
            switch (action) {
                case ALL_FILES_SENT -> {
                    log.debug("Инфо: Получены все файлы от клиента. Ищем, что не хватает");
                    //todo поиск файлов, которых нет у клиента

                }
            }


        }

        if (msg instanceof FileHeader) {
            FileHeader fileHeader = (FileHeader) msg;
            log.trace("Получен заголовок от клиента: " + fileHeader);
            filesHeadersListOnClient.add(fileHeader);

            //todo проверак файла в базе,
            //                  если нету,
            //                          записать,
            //                          запросить


        }
        if (msg instanceof FileMessage) {
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
