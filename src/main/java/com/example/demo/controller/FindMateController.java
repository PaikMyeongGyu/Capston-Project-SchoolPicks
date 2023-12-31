package com.example.demo.controller;

import com.example.demo.dto.FindMate.FindMateRoomDto;
import com.example.demo.dto.FindMate.FindMateRoomForm;
import com.example.demo.dto.FindMate.FindMateRoomPageDto;
import com.example.demo.dto.FindMate.FindMateRoomPageForm;
import com.example.demo.dto.ResponseDto;
import com.example.demo.entity.FindMate.RoomUser;
import com.example.demo.service.FindMateRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Random;


@Controller
@RequiredArgsConstructor
@Slf4j
public class FindMateController {

    private final FindMateRoomService findMateRoomService;

    @GetMapping("/mate")
    public String writeFindMateRoom(@ModelAttribute("findMateRoom") FindMateRoomForm findMateRoomForm, Model model
            , @Nullable HttpServletRequest request) {

        log.info("get Mapping /mate 여기");
        if(request != null){
            try{
                Cookie[] cookies = request.getCookies();
                if(cookies != null){
                    for(Cookie cookie: cookies){
                        String str = URLDecoder.decode(cookie.getValue(), "UTF-8");

                        if(cookie.getName().equals("prevName"))
                            findMateRoomForm.setRoomWriter(str);
                        else if(cookie.getName().equals("prevPassword"))
                            findMateRoomForm.setRoomPassword(str);
                    }
                }

            } catch (UnsupportedEncodingException e) {

            }
        }

        findMateRoomForm.setHeadCount(2);
        findMateRoomForm.setIsPrivate(String.valueOf(false));
        findMateRoomForm.setVersion(4);

        return "find-mate-";
    }

    @PostMapping("/mate")
    public String postFindMateRoom(@ModelAttribute("findMateRoom") FindMateRoomForm findMateRoomForm, Model model
            , HttpServletResponse httpServletResponse) {

        Cookie cookie = new Cookie("prevName", findMateRoomForm.getRoomWriter());
        cookie.setMaxAge(3600 * 12 * 7);

        Cookie cookie2 = new Cookie("prevPassword", findMateRoomForm.getRoomPassword());
        cookie2.setMaxAge(3600 * 12 * 7);

        httpServletResponse.addCookie(cookie);
        httpServletResponse.addCookie(cookie2);
        //로그 찍기
        logPostFindMateRoom(findMateRoomForm);

        boolean isPrivate;
        if (findMateRoomForm.getIsPrivate().equals("false")) {
            isPrivate = false;
        } else {
            isPrivate = true;
        }

        FindMateRoomDto findMateRoomDto = null;

        int version = findMateRoomForm.getVersion();
        if (version == 4) {
            version = (int) ((Math.random() * 10000) % 3) + 1;
        }
        log.info("version = " + version);

        try{
            findMateRoomDto = new FindMateRoomDto(
                    findMateRoomForm.getRoomTitle(),
                    findMateRoomForm.getShopName(),
                    LocalDateTime.parse(findMateRoomForm.getPlanTime()),
                    LocalDateTime.parse(findMateRoomForm.getExpiredTime()),
                    findMateRoomForm.getHeadCount(),
                    findMateRoomForm.getRoomWriter(),
                    findMateRoomForm.getRoomMessage(),
                    isPrivate,
                    findMateRoomForm.getRoomPassword(),
                    version
            );
        } catch(DateTimeParseException e){ // 시간값 제대로 입력 안한 경우 처리
            // 이거 나중에 출력하실 때 사용하세요.
            model.addAttribute("message", "시간 양식이 잘못되었습니다.");
            return "find-mate-";
        }

        ResponseDto<String> response = findMateRoomService.createFindMateRoom(findMateRoomDto);

        if(!response.getResult()) { // 시간값이 앞이거나 다른 값을 제대로 안 넣은 경우 처리
            // 이거 나중에 출력하실 때 사용하세요.
            model.addAttribute("message", response.getMessage());
            return "find-mate-";
        }

        String roomId = response.getData();
        model.addAttribute("roomId", roomId);

        if (version == 1) {
            return "redirect:/mate/room/ver1/" + roomId;
        } else if (version == 2) {
            return "redirect:/mate/room/ver2/" + roomId;
        } else if (version == 3) {
            return "redirect:/mate/room/ver3/" + roomId;
        } else {
            return "find-mate";
        }
    }

    /**
     * 게시글 ver123
     */
    @GetMapping("/mate/room/ver1/{roomId}")
    public String showFindMateRoomVer1(@PathVariable String roomId, @ModelAttribute("findMateRoomPage") FindMateRoomPageForm findMateRoomPageForm, Model model) {
        FindMateRoomPageDto findMateRoomPageDto = getFindMateRoomPageDtoDistinguishedPrivate(roomId);
        setDtoInForm(findMateRoomPageForm, findMateRoomPageDto);
        model.addAttribute("password", findMateRoomPageDto.getRoomPassword());
        model.addAttribute("addUser", new RoomUser());
        return "find-mate-ver1";
    }

    @GetMapping("/mate/room/ver2/{roomId}")
    public String showFindMateRoomVer2(@PathVariable String roomId, @ModelAttribute("findMateRoomPage") FindMateRoomPageForm findMateRoomPageForm, Model model) {
        FindMateRoomPageDto findMateRoomPageDto = getFindMateRoomPageDtoDistinguishedPrivate(roomId);
        setDtoInForm(findMateRoomPageForm, findMateRoomPageDto);
        model.addAttribute("password", findMateRoomPageDto.getRoomPassword());
        model.addAttribute("addUser", new RoomUser());
        return "find-mate-ver2";
    }

    @GetMapping("/mate/room/ver3/{roomId}")
    public String showFindMateRoomVer3(@PathVariable String roomId, @ModelAttribute("findMateRoomPage") FindMateRoomPageForm findMateRoomPageForm, Model model) {

        FindMateRoomPageDto findMateRoomPageDto = getFindMateRoomPageDtoDistinguishedPrivate(roomId);

        String localDateTime = findMateRoomPageDto.getLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-E-HH-mm-ss"));
        String[] localDateTimeSplit = localDateTime.split("-");
        String year = localDateTimeSplit[0];
        String month = localDateTimeSplit[1];
        String date = localDateTimeSplit[2];
        String day = "(" + localDateTimeSplit[3] + ")";
        int hour = Integer.parseInt(localDateTimeSplit[4]);
        String minute = localDateTimeSplit[5];
        String time;
        if (hour >= 12) {
            time = "오후 " + (hour - 12) + ":" + minute;
        } else {
            time = "오전 " + hour + ":" + minute;
        }

        String expiredTimeSentence = setExpiredTimeSentence(findMateRoomPageDto);
        String[] expiredTimeSentenceSplit = expiredTimeSentence.split("오");

        findMateRoomPageForm.setShopName(findMateRoomPageDto.getShopName());
        findMateRoomPageForm.setYear(year);
        findMateRoomPageForm.setMonth(month);
        findMateRoomPageForm.setDate(date);
        findMateRoomPageForm.setDay(day);
        findMateRoomPageForm.setTime(time);
        findMateRoomPageForm.setHeadCount(findMateRoomPageDto.getHeadCount());
        findMateRoomPageForm.setRoomWriter(findMateRoomPageDto.getRoomWriter());
        findMateRoomPageForm.setRoomMessage(findMateRoomPageDto.getRoomMessage());
        findMateRoomPageForm.setUsers(findMateRoomPageDto.getUsers());

        model.addAttribute("password", findMateRoomPageDto.getRoomPassword());
        model.addAttribute("addUser", new RoomUser());
        model.addAttribute("expiredTime1", expiredTimeSentenceSplit[0]);
        model.addAttribute("expiredTime2", "오" + expiredTimeSentenceSplit[1]);

        return "find-mate-ver3";
    }

    /*
    @GetMapping("/mate/room/ver4/{roomId}")
    public String showFindMateRoomVer4(@PathVariable String roomId, @ModelAttribute("findMateRoomPage") FindMateRoomPageForm findMateRoomPageForm, Model model) {

        FindMateRoomPageDto findMateRoomPageDto = getFindMateRoomPageDtoDistinguishedPrivate(roomId);

        String localDateTime = findMateRoomPageDto.getLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-E-HH-mm-ss"));
        String[] localDateTimeSplit = localDateTime.split("-");
        String year = localDateTimeSplit[0];
        String month = localDateTimeSplit[1];
        String date = localDateTimeSplit[2];
        String day = "(" + localDateTimeSplit[3] + ")";
        int hour = Integer.parseInt(localDateTimeSplit[4]);
        String minute = localDateTimeSplit[5];
        String time;
        if (hour >= 12) {
            time = "오후 " + (hour - 12) + ":" + minute;
        } else {
            time = "오전 " + hour + ":" + minute;
        }

        findMateRoomPageForm.setShopName(findMateRoomPageDto.getShopName());
        findMateRoomPageForm.setYear(year);
        findMateRoomPageForm.setMonth(month);
        findMateRoomPageForm.setDate(date);
        findMateRoomPageForm.setDay(day);
        findMateRoomPageForm.setTime(time);
        findMateRoomPageForm.setHeadCount(findMateRoomPageDto.getHeadCount());
        findMateRoomPageForm.setRoomWriter(findMateRoomPageDto.getRoomWriter());
        findMateRoomPageForm.setRoomMessage(findMateRoomPageDto.getRoomMessage());
        findMateRoomPageForm.setUsers(findMateRoomPageDto.getUsers());

        model.addAttribute("password", findMateRoomPageDto.getRoomPassword());
        model.addAttribute("addUser", new RoomUser());

        Random random = new Random();
        int randVersion = random.nextInt(3) + 1;
        return "find-mate-ver" + randVersion;
    }
    */


    /**
     * 수정하기
     * 기존에 있던 게시글을 리포지토리에서 삭제하고, 게시글을 만드는 페이지로 이동한다
     */
    @PostMapping("/mate/room/ver1/{roomId}")
    public String editFindMateRoomVer1(@PathVariable String roomId) {
        findMateRoomService.deleteFindMateRoom(roomId);
        return "redirect:/mate";
    }

    @PostMapping("/mate/room/ver2/{roomId}")
    public String editFindMateRoomVer2(@PathVariable String roomId) {
        findMateRoomService.deleteFindMateRoom(roomId);
        return "redirect:/mate";
    }

    @PostMapping("/mate/room/ver3/{roomId}")
    public String editFindMateRoomVer3(@PathVariable String roomId) {
        findMateRoomService.deleteFindMateRoom(roomId);
        return "redirect:/mate";
    }

    /**
     * 삭제하기
     */
    @PostMapping("/mate/room/ver1/delete/{roomId}")
    public String deleteFindMateRoomVer1(@PathVariable String roomId) {
        findMateRoomService.deleteFindMateRoom(roomId);
        return "redirect:/menu";
    }

    @PostMapping("/mate/room/ver2/delete/{roomId}")
    public String deleteFindMateRoomVer2(@PathVariable String roomId) {
        findMateRoomService.deleteFindMateRoom(roomId);
        return "redirect:/menu";
    }

    @PostMapping("/mate/room/ver3/delete/{roomId}")
    public String deleteFindMateRoomVer3(@PathVariable String roomId) {
        findMateRoomService.deleteFindMateRoom(roomId);
        return "redirect:/menu";
    }

    /**
     * 배고픈 사람 입력 (유저 추가하기)
     */
    @PostMapping("/mate/room/ver1/addUser/{roomId}")
    public String addUserFindMateRoomVer1(@PathVariable String roomId, @ModelAttribute("addUser") RoomUser addUser) {
        log.info("addUser.getUserName() = " + addUser.getUserName());
        log.info("roomId = " + roomId);

        if(addUser.getUserName().equals(""))
            return "redirect:/mate/room/ver1/" + roomId;

        findMateRoomService.joinFindMateRoom(addUser.getUserName(), roomId);

        return "redirect:/mate/room/ver1/" + roomId;
    }

    @PostMapping("/mate/room/ver2/addUser/{roomId}")
    public String addUserFindMateRoomVer2(@PathVariable String roomId, @ModelAttribute("addUser") RoomUser addUser) {
        if(addUser.getUserName().equals(""))
            return "redirect:/mate/room/ver2/" + roomId;
        findMateRoomService.joinFindMateRoom(addUser.getUserName(), roomId);
        return "redirect:/mate/room/ver2/" + roomId;
    }

    @PostMapping("/mate/room/ver3/addUser/{roomId}")
    public String addUserFindMateRoomVer3(@PathVariable String roomId, @ModelAttribute("addUser") RoomUser addUser) {
        if(addUser.getUserName().equals(""))
            return "redirect:/mate/room/ver3/" + roomId;
        findMateRoomService.joinFindMateRoom(addUser.getUserName(), roomId);
        return "redirect:/mate/room/ver3/" + roomId;
    }

    /**
     * 이름 보기
     * '이름보기' 버튼 클릭 -> 비밀번호 입력 -> 맞을시 이름 공개
     */
    @PostMapping("/mate/room/ver1/{password}/{roomId}")
    public String revealUserNameVer1(@PathVariable String password, @PathVariable String roomId, @ModelAttribute("findMateRoomPage") FindMateRoomPageForm findMateRoomPageForm, Model model) {
        FindMateRoomPageDto findMateRoomPageDto = findMateRoomService.showFindMateRoom(roomId);
        setDtoInForm(findMateRoomPageForm, findMateRoomPageDto);
        model.addAttribute("password", findMateRoomPageDto.getRoomPassword());
        model.addAttribute("addUser", new RoomUser());
        return "find-mate-ver1";
    }

    @PostMapping("/mate/room/ver2/{password}/{roomId}")
    public String revealUserNameVer2(@PathVariable String password, @PathVariable String roomId, @ModelAttribute("findMateRoomPage") FindMateRoomPageForm findMateRoomPageForm, Model model) {
        FindMateRoomPageDto findMateRoomPageDto = findMateRoomService.showFindMateRoom(roomId);
        setDtoInForm(findMateRoomPageForm, findMateRoomPageDto);
        model.addAttribute("password", findMateRoomPageDto.getRoomPassword());
        model.addAttribute("addUser", new RoomUser());
        return "find-mate-ver2";
    }

    @PostMapping("/mate/room/ver3/{password}/{roomId}")
    public String revealUserNameVer3(@PathVariable String password, @PathVariable String roomId, @ModelAttribute("findMateRoomPage") FindMateRoomPageForm findMateRoomPageForm, Model model) {
        FindMateRoomPageDto findMateRoomPageDto = findMateRoomService.showFindMateRoom(roomId);

        String localDateTime = findMateRoomPageDto.getLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-E-HH-mm-ss"));
        String[] localDateTimeSplit = localDateTime.split("-");
        String year = localDateTimeSplit[0];
        String month = localDateTimeSplit[1];
        String date = localDateTimeSplit[2];
        String day = "(" + localDateTimeSplit[3] + ")";
        int hour = Integer.parseInt(localDateTimeSplit[4]);
        String minute = localDateTimeSplit[5];
        String time;
        if (hour >= 12) {
            time = "오후 " + (hour - 12) + ":" + minute;
        } else {
            time = "오전 " + hour + ":" + minute;
        }

        String expiredTimeSentence = setExpiredTimeSentence(findMateRoomPageDto);
        String[] expiredTimeSentenceSplit = expiredTimeSentence.split("오");

        findMateRoomPageForm.setShopName(findMateRoomPageDto.getShopName());
        findMateRoomPageForm.setYear(year);
        findMateRoomPageForm.setMonth(month);
        findMateRoomPageForm.setDate(date);
        findMateRoomPageForm.setDay(day);
        findMateRoomPageForm.setTime(time);
        findMateRoomPageForm.setHeadCount(findMateRoomPageDto.getHeadCount());
        findMateRoomPageForm.setRoomWriter(findMateRoomPageDto.getRoomWriter());
        findMateRoomPageForm.setRoomMessage(findMateRoomPageDto.getRoomMessage());
        findMateRoomPageForm.setUsers(findMateRoomPageDto.getUsers());

        model.addAttribute("password", findMateRoomPageDto.getRoomPassword());
        model.addAttribute("addUser", new RoomUser());
        model.addAttribute("expiredTime1", expiredTimeSentenceSplit[0]);
        model.addAttribute("expiredTime2", "오" + expiredTimeSentenceSplit[1]);

        return "find-mate-ver3";
    }



    /**
     * 인스타그램 사진 페이지
     */
    @GetMapping("/mate/instagram/ver1/{roomId}")
    public String shareInstagramVer1(@PathVariable String roomId, @ModelAttribute("findMateRoomPage") FindMateRoomPageForm findMateRoomPageForm, Model model) {
        FindMateRoomPageDto findMateRoomPageDto = getFindMateRoomPageDtoDistinguishedPrivate(roomId);
        setDtoInForm(findMateRoomPageForm, findMateRoomPageDto);
        model.addAttribute("roomId", roomId);
        return "ver1-instagram-story";
    }

    @GetMapping("/mate/instagram/ver2/{roomId}")
    public String shareInstagramVer2(@PathVariable String roomId, @ModelAttribute("findMateRoomPage") FindMateRoomPageForm findMateRoomPageForm, Model model) {
        FindMateRoomPageDto findMateRoomPageDto = getFindMateRoomPageDtoDistinguishedPrivate(roomId);
        setDtoInForm(findMateRoomPageForm, findMateRoomPageDto);
        model.addAttribute("roomId", roomId);
        return "ver2-instagram-story";
    }

    @GetMapping("/mate/instagram/ver3/{roomId}")
    public String shareInstagramVer3(@PathVariable String roomId, @ModelAttribute("findMateRoomPage") FindMateRoomPageForm findMateRoomPageForm, Model model) {

        FindMateRoomPageDto findMateRoomPageDto = getFindMateRoomPageDtoDistinguishedPrivate(roomId);

        String localDateTime = findMateRoomPageDto.getLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-E-HH-mm-ss"));
        String[] localDateTimeSplit = localDateTime.split("-");
        String year = localDateTimeSplit[0];
        String month = localDateTimeSplit[1];
        String date = localDateTimeSplit[2];
        String day = "(" + localDateTimeSplit[3] + ")";
        int hour = Integer.parseInt(localDateTimeSplit[4]);
        String minute = localDateTimeSplit[5];
        String time;
        if (hour >= 12) {
            time = "오후 " + (hour - 12) + ":" + minute;
        } else {
            time = "오전 " + hour + ":" + minute;
        }

        String expiredTimeSentence = setExpiredTimeSentence(findMateRoomPageDto);
        String[] expiredTimeSentenceSplit = expiredTimeSentence.split("오");

        findMateRoomPageForm.setShopName(findMateRoomPageDto.getShopName());
        findMateRoomPageForm.setYear(year);
        findMateRoomPageForm.setMonth(month);
        findMateRoomPageForm.setDate(date);
        findMateRoomPageForm.setDay(day);
        findMateRoomPageForm.setTime(time);
        findMateRoomPageForm.setHeadCount(findMateRoomPageDto.getHeadCount());
        findMateRoomPageForm.setRoomWriter(findMateRoomPageDto.getRoomWriter());
        findMateRoomPageForm.setRoomMessage(findMateRoomPageDto.getRoomMessage());
        findMateRoomPageForm.setUsers(findMateRoomPageDto.getUsers());
        findMateRoomPageForm.setExpiredTime(expiredTimeSentence);

        model.addAttribute("roomId", roomId);
        model.addAttribute("expiredTime1", expiredTimeSentenceSplit[0]);
        model.addAttribute("expiredTime2", "오" + expiredTimeSentenceSplit[1]);

        return "ver3-instagram-story";
    }






    private static void logPostFindMateRoom(FindMateRoomForm findMateRoomForm) {
        log.info("=== Post 완료! ===");
        log.info("게시글 제목 = " + findMateRoomForm.getRoomTitle());
        log.info("음식점 이름 = " + findMateRoomForm.getShopName());
        log.info("날짜 및 시간 = " + findMateRoomForm.getPlanTime());
        log.info("인원수 = " + findMateRoomForm.getHeadCount());
        log.info("게시글 작성자 = " + findMateRoomForm.getRoomWriter());
        log.info("작성자의 메시지 = " + findMateRoomForm.getRoomMessage());
        log.info("친구이름 공개/비공개 = " + findMateRoomForm.getIsPrivate());
        log.info("게시글 수정 비밀번호(4자리) = " + findMateRoomForm.getRoomPassword());
    }

    private static String setExpiredTimeSentence(FindMateRoomPageDto findMateRoomPageDto) {
        String expiredTime = findMateRoomPageDto.getExpiredTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-E-HH-mm-ss"));
        String[] expiredTimeSplit = expiredTime.split("-");
        String expiredTimeSentence = expiredTimeSplit[0] + "년 " + expiredTimeSplit[1] + "월 " + expiredTimeSplit[2] + "일 (" + expiredTimeSplit[3] + ") ";
        int expiredHour = Integer.parseInt(expiredTimeSplit[4]);
        if (expiredHour >= 12) {
            expiredTimeSentence += "오후 " + (expiredHour - 12) + ":" + expiredTimeSplit[5];
        } else {
            expiredTimeSentence += "오전" + expiredHour + ":" + expiredTimeSplit[5];
        }
        return expiredTimeSentence;
    }

    private static void setDtoInForm(FindMateRoomPageForm findMateRoomPageForm, FindMateRoomPageDto findMateRoomPageDto) {
        setDateTimeInForm(findMateRoomPageForm, findMateRoomPageDto);
        String expiredTimeSentence = setExpiredTimeSentence(findMateRoomPageDto);

        findMateRoomPageForm.setShopName(findMateRoomPageDto.getShopName());
        findMateRoomPageForm.setHeadCount(findMateRoomPageDto.getHeadCount());
        findMateRoomPageForm.setRoomWriter(findMateRoomPageDto.getRoomWriter());
        findMateRoomPageForm.setRoomMessage(findMateRoomPageDto.getRoomMessage());
        findMateRoomPageForm.setUsers(findMateRoomPageDto.getUsers());
        findMateRoomPageForm.setExpiredTime(expiredTimeSentence);
    }

    private static void setDateTimeInForm(FindMateRoomPageForm findMateRoomPageForm, FindMateRoomPageDto findMateRoomPageDto) {
        String localDateTime = findMateRoomPageDto.getLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-E-HH-mm-ss"));
        String[] localDateTimeSplit = localDateTime.split("-");
        String year = localDateTimeSplit[0];
        String month = localDateTimeSplit[1];
        String date = localDateTimeSplit[2];
        String day = "(" + localDateTimeSplit[3] + ")";
        int hour = Integer.parseInt(localDateTimeSplit[4]);
        String minute = localDateTimeSplit[5];
        String time;
        if (hour >= 12) {
            time = "오후 " + (hour - 12) + ":" + minute;
        } else {
            time = "오전 " + hour + ":" + minute;
        }
        findMateRoomPageForm.setYear(year);
        findMateRoomPageForm.setMonth(month);
        findMateRoomPageForm.setDate(date);
        findMateRoomPageForm.setDay(day);
        findMateRoomPageForm.setTime(time);
    }

    private FindMateRoomPageDto getFindMateRoomPageDtoDistinguishedPrivate(String roomId) {
        FindMateRoomPageDto findMateRoomPageDto;
        if (findMateRoomService.getIsPrivate(roomId)) {
            findMateRoomPageDto = findMateRoomService.showFindMateRoomWithBlindMode(roomId);
        } else {
            findMateRoomPageDto = findMateRoomService.showFindMateRoom(roomId);
        }
        return findMateRoomPageDto;
    }



}
