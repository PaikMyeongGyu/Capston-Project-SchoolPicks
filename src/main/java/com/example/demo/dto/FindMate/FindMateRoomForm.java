package com.example.demo.dto.FindMate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindMateRoomForm {

    String roomTitle;
    String shopName;
    String planTime;
    String expiredTime;
    int headCount;
    String roomWriter;
    String roomMessage;
    String isPrivate;
    String roomPassword;

    int version;
}
