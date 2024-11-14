package org.packet.reactivewebapp.services;



import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.reactivestreams.client.gridfs.GridFSBucket;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Service;



import org.springframework.http.codec.multipart.FilePart;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

@Service
@AllArgsConstructor
public class ServiceForAudio {

    private final GridFSBucket gridFSBucket;

    public Mono<ObjectId> saveAudio(FilePart filePart) {
        // Указываем опции загрузки с метаданными
        GridFSUploadOptions options = new GridFSUploadOptions()
                .metadata(new org.bson.Document("type", "audio")
                        .append("contentType", filePart.headers().getContentType().toString()));

        // Преобразуем DataBuffer в ByteBuffer, чтобы GridFSBucket смог его обработать
        Flux<ByteBuffer> byteBufferFlux = filePart.content()
                .map(DataBuffer::toByteBuffer);  // Преобразуем каждый DataBuffer в ByteBuffer
        System.out.println( options.getMetadata().toString());

        // Используем метод uploadFromPublisher, который принимает Publisher<ByteBuffer>
        return Mono.from(gridFSBucket.uploadFromPublisher(filePart.filename(), byteBufferFlux)).
                doOnError(
                error->System.out.println(error.getMessage())
                 )
                .doOnSuccess(System.out::println);
    }
}

