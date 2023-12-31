package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor(staticName="set")
public class ResponseDto<D> {

    private boolean result;
    private String message;
    private D data;

    // 성공했을 때 정의하는 메서드
    public static <D> ResponseDto<D> setSuccess(String message, D data){
        return ResponseDto.set(true, message, data);
    }

    // 실패했을 때 정의하는 메서드
    public static <D> ResponseDto<D> setFailed(String message){
        return ResponseDto.set(false, message, null);
    }

    public boolean getResult(){
        return this.result;
    }
}
