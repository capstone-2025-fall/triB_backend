package triB.triB.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import triB.triB.room.repository.RoomRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final RoomRepository roomRepository;

    public List<Long> makeTrips(Long roomId){
        return null;
    }

}
