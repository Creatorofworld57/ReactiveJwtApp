package org.packet.reactivewebapp.Controllers;


import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.reactivestreams.client.gridfs.GridFSBucket;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.packet.reactivewebapp.Entities.Audio;
import org.packet.reactivewebapp.Repositories.AudioRepository;
import org.packet.reactivewebapp.services.ServiceForAudio;
import org.reactivestreams.Publisher;

import org.springframework.core.io.buffer.DataBuffer;

import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RestControllerForAudio {

    private final GridFSBucket gridFSBucket;

    private final ServiceForAudio service;

    private final AudioRepository audioRepository;


    @GetMapping("/audio/{id}")
    public Mono<ResponseEntity<Flux<DataBuffer>>> getAudio(@PathVariable String id) {
        // Ищем файл по его идентификатору
        Publisher<GridFSFile> gridFSFilePublisher = gridFSBucket.find(new Document("_id", new ObjectId(id))).first();

        return Mono.from(gridFSFilePublisher)
                .flatMap(gridFSFile -> {
                    // Если файл не найден, возвращаем 404
                    if (gridFSFile == null) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }

                    // Проверка на null для метаданных
                    org.bson.Document metadata = gridFSFile.getMetadata();
                    String contentType = (metadata != null && metadata.getString("contentType") != null)
                            ? metadata.getString("contentType")
                            : MediaType.APPLICATION_OCTET_STREAM_VALUE;  // Установите тип по умолчанию

                    // Загружаем содержимое файла в виде Flux<DataBuffer>
                    Flux<DataBuffer> dataBufferFlux = Flux.from(gridFSBucket.downloadToPublisher(gridFSFile.getObjectId()))
                            .map(ByteBuffer::array)  // Преобразуем ByteBuffer в массив байтов
                            .map(bytes -> {
                                return (DataBuffer) new DefaultDataBufferFactory().wrap(bytes);  // Заворачиваем байты в DataBuffer
                            });

                    // Возвращаем файл с корректными заголовками
                    return Mono.just(ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + gridFSFile.getFilename() + "\"")
                            .body(dataBufferFlux));
                });
    }



    @PostMapping("/audio")
    public Mono<ResponseEntity<String>> uploadAudio(@RequestPart("file") FilePart file) {
        // Проверка файла на наличие и тип
        if (file == null || !file.filename().endsWith(".mp3")) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid file format. Only .mp3 files are accepted."));
        }

        Audio audio = new Audio();

        // Сохранение аудио-файла
        return service.saveAudio(file)
                .flatMap(fileId -> {
                    audio.setIdOfTrack(fileId);

                    // Сохранение данных в базе
                    return audioRepository.save(audio)
                            .map(savedAudio -> ResponseEntity.ok("Audio saved with ID: " + savedAudio.getIdOfTrack().toString()))
                            .doOnSuccess(saveAudio->System.out.println(saveAudio.getHeaders()));
                })
                .onErrorResume(e -> {
                    // Возвращение сообщения об ошибке
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save audio"));
                });
    }

}
