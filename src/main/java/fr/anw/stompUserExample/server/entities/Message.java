package fr.anw.stompUserExample.server.entities;


import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Message {
    private String sender;
    private String content;
    private Boolean haveIt;
}
