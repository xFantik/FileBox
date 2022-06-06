package ru.bp.fileBoxServer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;
import ru.pb.fileBoxCommon.messages.FileMessage;
import ru.pb.fileBoxCommon.messages.FileRequest;
import ru.pb.fileBoxCommon.messages.InfoMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
public class MainHandler extends ChannelInboundHandlerAdapter {

    private static String rootDirectory = "server_storage";
    ExecutorService executorService;
    private int waitingFileCountInList;
    private String userName = "fant";

    private List<FileMessage> filesHeadersListOnClient;
    private List<FileMessage> filesHeadersListOnServer;

    public MainHandler() {
        System.out.println("СОЗДАН Экзекьютор");
        this.executorService = Executors.newSingleThreadExecutor();
        filesHeadersListOnServer = MySQLService.getDataBaseFileList(userName);
        filesHeadersListOnClient = new ArrayList<>();

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws MySQLConnectException, IOException {
//        if (msg instanceof FileRequest) {
//            FileRequest fr = (FileRequest) msg;
//            if (Files.exists(Paths.get("server_storage/", userName, fr.getFilename()))) {
//                FileMessage fm = new FileMessage(Paths.get("server_storage/", userName, fr.getFilename()), true);
//                ctx.writeAndFlush(fm);
//            }
//        }

        if (msg instanceof InfoMessage) {
            InfoMessage.MessageType action = ((InfoMessage) msg).getMessageType();
            switch (action) {
                case ALL_FILES_SENT -> {
                    log.debug("Инфо: Получены все файлы от клиента. Ищем, что не хватает");
                    //todo поиск файлов, которых нет у клиента

                }
            }
        }


        if (msg instanceof FileMessage) {
            FileMessage fm = (FileMessage) msg;
            if (fm.getHash() == null) {                     // Если приехал только заголовок
                log.trace(userName + ": Получен заголовок от клиента: " + fm);

                try {
                    long fileTimeOnServer = MySQLService.getFileTime(userName, fm.getFilePath());

                    System.out.println(fileTimeOnServer);
                    System.out.println(fm.getLastModifiedSeconds());
                    if (fm.getLastModifiedSeconds() > fileTimeOnServer) {
                        log.debug(userName + ": Устаревший файл на сервере: " + fm);
                        ctx.writeAndFlush(new FileRequest(fm.getFilePath()));
                    } else if (fm.getLastModifiedSeconds() < fileTimeOnServer) {
                        //todo отправка клиенту новой версии файла (или команду на удаление)
                        addHeaderToList(fm);

                    } else {
                        addHeaderToList(fm);
                    }

                } catch (DBFileNotFoundException e) { //файл в БД не найден
                    MySQLService.addFileHeader(userName, fm);
                    log.debug("Запросили у "+userName+" "+ fm.getFilePath());
                    ctx.writeAndFlush(new FileRequest(fm.getFilePath()));

                }
            } else {                                                  //если приехало тело файла

                log.debug(userName + ": Пришли данные файла: " + fm.getFilePath());

                long id=0;
                try {
                    Path p = Paths.get(fm.getFilePath());
                    id = MySQLService.getFileID(userName, p.subpath(1,p.getNameCount()).toString());
                } catch (DBFileNotFoundException e) {
                    e.printStackTrace();
                }

                Path localPath = Paths.get(rootDirectory, String.valueOf(id));
                Files.write(localPath, fm.getData(), StandardOpenOption.CREATE);


                //todo проверить хэш

                MySQLService.addFileSuccess(id, fm.getHash(), fm.getLastModifiedSeconds());


            }
        }


    }

    private boolean addHeaderToList(FileMessage fm) {
        for (FileMessage o : filesHeadersListOnClient) {
            if (fm.equals(o)) {
                return false;
            }
        }
        filesHeadersListOnClient.add(fm);
        return true;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
