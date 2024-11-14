package org.packet.reactivewebapp.Entities;


import lombok.Data;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;
import java.util.UUID;

@Data
@Document
public class User {

    @Id
   private UUID id;
    @Indexed(unique=true)
   private  String name;

   private Date created_At;

   private Date updated_At;
   private String roles;

   private String password;


}
