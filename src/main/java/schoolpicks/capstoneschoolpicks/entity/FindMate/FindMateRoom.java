package schoolpicks.capstoneschoolpicks.entity.FindMate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@Entity
@AllArgsConstructor // constructor 생성
@NoArgsConstructor // getter, setter 생성
public class FindMateRoom {

    @Id @GeneratedValue
    @Column(name = "FIND_MATE_ROOM_ID")
    private Long id;

    String roomId;

    String roomTitle;
    String shopName;

    @DateTimeFormat(pattern = "yyyy-MM-dd/HH:mm:ss")
    private LocalDateTime planTime;

    int headCount;

    String roomWriter;

    @Column(columnDefinition = "TEXT")
    private String roomMessage;

    boolean isPrivate;

    String roomPassword;

    @OneToMany(mappedBy = "findMateRoom")
    private List<RoomUser> roomUsers = new ArrayList<>();

    @OneToMany(mappedBy = "findMateRoom")
    private List<RoomUserForGroup> roomUserForGroups = new ArrayList<>();
}
