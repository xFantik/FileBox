package ru.bp.fileBoxServer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;
import ru.pb.fileBoxCommon.messages.*;
import ru.pb.fileBoxCommon.utils.FileUtil;

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

    private static String rootDirectory;
    private ExecutorService executorService;
    private String userName = "fant";
    private Path userStorage;

    private List<FileMessage> filesHeadersListOnClient;
    private List<FileMessage> filesHeadersListOnServer;

    static {
        rootDirectory = PropertyReader.getInstance().getStoragePath();
        try {
            Files.createDirectories(Path.of(rootDirectory));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MainHandler() throws IOException {
        this.executorService = Executors.newSingleThreadExecutor();
        userStorage = Paths.get(rootDirectory, userName);
        Files.createDirectories(userStorage);
        filesHeadersListOnServer = FileUtil.getFileList(userStorage);
        filesHeadersListOnClient = new ArrayList<>();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws MySQLConnectException, IOException {
        if (msg instanceof FileHeaderList) {
            filesHeadersListOnClient = ((FileHeaderList) msg).getFileList();
            updateFileList(ctx);

        }
        if (msg instanceof InfoMessage) {
            InfoMessage.MessageType action = ((InfoMessage) msg).getMessageType();
            switch (action) {
                case ALL_FILES_SENT -> {    //уже не нужно
                    log.debug("Инфо: Получены все файлы от клиента. Ищем, что не хватает");
                    for (FileMessage fileMessageOnServ : filesHeadersListOnServer) {
                        boolean find = false;
                        for (FileMessage fileMessageOnClient : filesHeadersListOnClient) {
                            if (fileMessageOnClient.equals(fileMessageOnServ)) {
                                find = true;
                                break;
                            }
                        }
                        if (!find) {
                            FileMessage fileMessage = new FileMessage(userStorage, fileMessageOnServ.getFilePath(), true);
                            log.debug("Запрашиваем у " + userName + " файл " + fileMessage.getFilePath().toString());
                            ctx.writeAndFlush(fileMessage);
                        }
                    }
                    filesHeadersListOnClient.clear();
                }
                case DELETE_FILE -> {
                    log.debug("Пришел запрос на удаление файла " + ((InfoMessage) msg).getMessage());
                    Path p = userStorage.resolve(((InfoMessage) msg).getMessage());
                    p.toFile().delete();
                    //todo запись в базу метки об удалении
                }
            }
        }


        if (msg instanceof FileMessage) {
            FileMessage fm = (FileMessage) msg;
            Path serverFilePath = userStorage.resolve(fm.getFilePath());
            if (fm.getHash() == null) {                     // Если приехал только заголовок
                log.trace(userName + ": Получен заголовок от клиента: " + fm);
                if (serverFilePath.toFile().exists()) {             //если файл существует
                    long fileTimeOnServer = FileUtil.getFileTime(serverFilePath);
//                    System.out.println(" время файла в заголовке: "+ fm.getLastModifiedSeconds()+"\n              на сервере: "+ fileTimeOnServer);
                    if (fm.getLastModifiedSeconds() > fileTimeOnServer) {
                        log.debug("Запросили у " + userName + " " + fm.getFilePath() + " (устаревший файл на сервере)");
                        ctx.writeAndFlush(new FileRequest(fm.getFilePath().toString()));
                    } else if (fm.getLastModifiedSeconds() < fileTimeOnServer) {
                        log.debug("Отправили " + userName + " " + fm.getFilePath() + " (устаревший файл на клиенте)");

                        addHeaderToList(fm);
                        FileMessage fileMessage = new FileMessage(userStorage, fm.getFilePath(), true);
                        ctx.writeAndFlush(fileMessage);

                        //todo отправка клиенту новой версии файла (или команду на удаление)

                    } else {
                        addHeaderToList(fm);
                    }

                } else {
                    log.debug("Запросили у " + userName + " " + fm.getFilePath() + " (файл отсутсвует на сервере)");
                    ctx.writeAndFlush(new FileRequest(fm.getFilePath().toString()));


                }
            } else {                                                  //если приехало тело файла

                log.debug(userName + ": Пришли данные файла: " + fm.getFilePath());
                Files.createDirectories(serverFilePath.subpath(0, serverFilePath.getNameCount() - 1));
                Files.write(serverFilePath, fm.getData(), StandardOpenOption.CREATE);
                FileUtil.setFileTime(serverFilePath, fm.getLastModifiedSeconds());

                //todo проверить хэш


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


    private void updateFileList(ChannelHandlerContext ctx) throws IOException {

        for (FileMessage newFileMessage : filesHeadersListOnClient) {
            if (!findHeaderInOldFileList(ctx, newFileMessage)) {
                ctx.writeAndFlush(new FileRequest(newFileMessage.getFilePath().toString()));
//                readAndSendFile(ctx, userStorage, newFileMessage.getFilePath());
            }
        }

        //todo поискать в базе удаленные файлы, чтобы сообщить клиентк
        for (FileMessage fileMessage : filesHeadersListOnServer) {
            ctx.writeAndFlush(new FileRequest(fileMessage.getFilePath().toString()));
                    }

        filesHeadersListOnServer = filesHeadersListOnClient;


    }

    private boolean findHeaderInOldFileList(ChannelHandlerContext ctx, FileMessage newFileMessage) throws IOException {
        for (FileMessage oldFileMessage : filesHeadersListOnServer) {
            if (oldFileMessage.equals(newFileMessage)) {

                if (oldFileMessage.getLastModifiedSeconds() < newFileMessage.getLastModifiedSeconds()) {
                    ctx.writeAndFlush(new FileRequest(newFileMessage.getFilePath().toString()));
                } else if (oldFileMessage.getLastModifiedSeconds() > newFileMessage.getLastModifiedSeconds()) {
                    readAndSendFile(ctx, userStorage, newFileMessage.getFilePath());
                }
                filesHeadersListOnServer.remove(oldFileMessage);
                return true;
            }
        }
        return false;

    }

    private static void readAndSendFile(ChannelHandlerContext ctx, Path storage, Path file) throws IOException {
        FileMessage fm = new FileMessage(storage, file, true);
        ctx.writeAndFlush(fm);
    }
}
