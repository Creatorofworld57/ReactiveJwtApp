package org.packet.reactivewebapp.Entities;



import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.UUID;

@Document
@Data
public class Audio implements Serializable {

    @Id
    private Long id;

    private String name;

    private String ContentType;

    private Long Size;

    private String Author;

    private String image;

    private ObjectId idOfTrack ;


    public Audio() {

    }
}
